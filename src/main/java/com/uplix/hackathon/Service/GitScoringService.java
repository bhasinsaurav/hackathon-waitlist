package com.uplix.hackathon.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uplix.hackathon.Dto.*;
import com.uplix.hackathon.Entity.JobScore;
import com.uplix.hackathon.Enum.ServiceStatus;
import com.uplix.hackathon.Repository.JobScoreRepo;
import com.uplix.hackathon.Util.NoiseFilter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Service

public class GitScoringService {


    private final RestClient restClient;
    private final ChatClient chatClient;

    private final JobScoreRepo jobScoreRepo;

    private String defaultBranch;
    private String ownerUri;

    public GitScoringService(RestClient restClient, ChatClient.Builder chatClientBuilder, JobScoreRepo jobScoreRepo) {
        this.restClient = restClient;
        this.chatClient = chatClientBuilder.build();
        this.jobScoreRepo = jobScoreRepo;
    }

    @Transactional
    @JmsListener(destination = "uplix/request/score/repo", containerFactory = "jmsListenerContainerFactory")
    public void calculateGitScore(String json) throws JsonProcessingException {
        GitJmsListener gitJmsListener = new ObjectMapper().readValue(json, GitJmsListener.class);
        String gitUrl = gitJmsListener.repoUrl();
        System.out.println(gitUrl);
        HashMap<String, Object> gitApis = fetchGitApis(gitUrl);
        String treesUrl = gitApis.get("trees_url").toString();
        getRepoScore(treesUrl, gitJmsListener.jobId());
    }

    private HashMap<String, Object> fetchGitApis(String gitUrl){
        int index = gitUrl.indexOf("github.com/");
        if(index == -1) return null;
        String repoPath = gitUrl.substring(index + "github.com/".length());
        this.ownerUri = "repos/" + repoPath;
        HashMap uri1 = restClient.get()
                .uri(ownerUri)
                .retrieve()
                .body(HashMap.class);
        this.defaultBranch = uri1.get("default_branch").toString();
        return uri1;
    }

    private void getRepoScore(String treesUrl, UUID jobId) {

        String uri = treesUrl.replace("{/sha}", "/" + defaultBranch + "?recursive=1");
        GitTreeResponse mainFolder = restClient.get()
                .uri(uri)
                .retrieve()
                .body(GitTreeResponse.class);

        List<GitTreeItem> list = mainFolder.tree().stream()
                .filter(treeItem -> !NoiseFilter.isNoise(treeItem.path()))
                .toList();

        List<String> paths = list.stream()
                .map(GitTreeItem::path)
                .toList();

        String Prompt = """
                You are a senior software reviewer. I will provide you with a flat list of all files in a project repository.

                Your tasks:
                1. Analyze the **project structure** and return a **score out of 10**.
                   - Consider organization, naming conventions, modularity, separation of concerns, and presence of documentation/tests/configuration.
                   - Provide a short explanation (max 2 sentences).
                
                2. Identify **exactly 3 files** that most likely contain the core business logic.
                   - Business logic means domain-specific rules, workflows, or key processing.
                   - Exclude boilerplate, configuration, migrations, entities/DTOs, generated code, and tests.
                   - Prefer service classes, domain logic handlers, or workflow-related files over controllers unless no better option exists.
                
                Return ONLY valid JSON in the following format:
                
                {
                  "project_structure_score": <number between 0-10>,
                  "project_structure_reasoning": "<short explanation>",
                  "recommended_business_logic_files": [
                    {
                      "file": "<path/filename>",
                      "justification": "<one-line reason>"
                    },
                    {
                      "file": "<path/filename>",
                      "justification": "<one-line reason>"
                    },
                    {
                      "file": "<path/filename>",
                      "justification": "<one-line reason>"
                    }
                  ]
                }
                
                Here is the project file list:
                
                """ + paths;

        ProjectReviewResponse aiResponse = aiResponse(Prompt, ProjectReviewResponse.class);
        int repoScore = aiResponse.project_structure_score();
        System.out.println("Repo Score: " + repoScore);
        System.out.println("Repo Reasoning: " + aiResponse.project_structure_reasoning());
        JobScore referenceById = jobScoreRepo.getReferenceById(jobId);
        referenceById.setCodeStructureScore(repoScore);
        referenceById.setCodeStructureScoreDescription(aiResponse.project_structure_reasoning());
        jobScoreRepo.save(referenceById);
        List<ProjectReviewResponse.BusinessLogicFile> businessLogicFiles = aiResponse.recommended_business_logic_files();

        List<GitTreeItem> tree = mainFolder.tree();
        ArrayList<String> filesContent = new ArrayList<>();

        for(ProjectReviewResponse.BusinessLogicFile businessLogicFile : businessLogicFiles){
            String filePath = businessLogicFile.file();
            System.out.println("File Path: " + filePath);
            for(GitTreeItem gitTreeItem : tree){
                if(gitTreeItem.path().equals(filePath)){
                    String sha = gitTreeItem.sha();
                    String fileApiUrl = ownerUri + "/git/blobs/" + sha;
                    BusinessFileContent fileContent = restClient.get()
                            .uri(fileApiUrl)
                            .retrieve()
                            .body(BusinessFileContent.class);
                    String decode = decode(fileContent.content());
                    String codeFile = filePreProcess(decode);
                    filesContent.add(codeFile);
                    break;

                }
            }
        }

        String filePrompt = """
                You are a senior software reviewer. I will provide you with the content of 3 files from a project repository that likely contain core business logic.

                Your task:
                1. Analyze the provided code snippets and return a **code quality score out of 10**.
                   - Consider readability, maintainability, adherence to best practices, and overall code quality.
                   - Provide a short explanation (max 2 sentences).

                Return ONLY valid JSON in the following format:

                {
                  "code_quality_score": <number between 0-10>,
                  "code_quality_reasoning": "<short explanation>"
                }

                Here are the code snippets:

                """ + filesContent;

        CodeReviewResponse codeReviewResponse = aiResponse(filePrompt, CodeReviewResponse.class);
        int codeScore = codeReviewResponse.code_quality_score();
        System.out.println("Code Score: " + codeScore);
        System.out.println("Code Reasoning: " + codeReviewResponse.code_quality_reasoning());
        JobScore user = jobScoreRepo.getReferenceById(jobId);
        user.setCodeComplexityScore(codeScore);
        user.setCodeComplexityDescription(codeReviewResponse.code_quality_reasoning());
        user.setRepoScoreStatus(ServiceStatus.COMPLETED);
        jobScoreRepo.save(user);

    }


    @Transactional
    @JmsListener(destination = "uplix/request/score/repo", containerFactory = "jmsListenerContainerFactory")
    public void getReadmeScore(String json) throws JsonProcessingException {
        GitJmsListener gitJmsListener = new ObjectMapper().readValue(json, GitJmsListener.class);
        String gitUrl = gitJmsListener.repoUrl();
        int index = gitUrl.indexOf("github.com/");
        if(index == -1) return ;
        String repoPath = gitUrl.substring(index + "github.com/".length());
        this.ownerUri = "repos/" + repoPath;
        String readmeUrl = ownerUri + "/readme";
        GitReadmeResponse readme = restClient.get()
                .uri(readmeUrl)
                .retrieve()
                .body(GitReadmeResponse.class);
        String md = decode(readme.content());
        String preprocess = preprocess(md);
        String prompt = """
                You are a senior software reviewer. I will provide you with the README.md content of a project.

                Your task:
                1. Analyze the **README quality** and return a **score out of 10**.
                   - Consider clarity, completeness, structure, and usefulness for understanding and using the project.
                   - Provide a short explanation (max 2 sentences).

                Return ONLY valid JSON in the following format:

                {
                  "readme_quality_score": <number between 0-10>,
                  "readme_quality_reasoning": "<short explanation>"
                }

                Here is the README content:

                """ + preprocess;
        ReadmeReviewResponse aiResponse = aiResponse(prompt, ReadmeReviewResponse.class);
        int readmeScore = aiResponse.readme_quality_score();
        System.out.println("Readme Score: " + readmeScore);
        System.out.println("Readme Reasoning: " + aiResponse.readme_quality_reasoning());
        JobScore referenceById = jobScoreRepo.getReferenceById(gitJmsListener.jobId());
        referenceById.setReadmeScore(readmeScore);
        referenceById.setReadmeScoreDescription(aiResponse.readme_quality_reasoning());
        referenceById.setReadmeScoreStatus(ServiceStatus.COMPLETED);
        jobScoreRepo.save(referenceById);

    }

    String preprocess(String md) {
        // Remove badges/images
        String s = md.replaceAll("!\\[[^]]*\\]\\([^)]*\\)", "");
        // Collapse code blocks
        s = s.replaceAll("(?s)```.*?```", "[code]");
        // Keep first ~2500 chars
        return s.length() > 2500 ? s.substring(0, 2500) : s;
    }

    String decode (String encoded){
        byte[] decode = Base64.getMimeDecoder().decode(encoded);
        return new String(decode);
    }
    <T> T aiResponse(String prompt, Class<T> returnType) {
        UserMessage promptMessage = new UserMessage(prompt);

        Prompt AIPrompt = new Prompt(promptMessage);
        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(returnType);

    }

    String filePreProcess(String code) {
        // Remove images/badges in case of markdown
        String s = code.replaceAll("!\\[[^]]*\\]\\([^)]*\\)", "");

        // Collapse code blocks in markdown
        s = s.replaceAll("(?s)```.*?```", "[code]");

        // Remove long string literals
        s = s.replaceAll("(?s)\"(?:[^\"\\\\]|\\\\.){100,}\"", "\"[long_string]\"");
        s = s.replaceAll("(?s)'(?:[^'\\\\]|\\\\.){100,}'", "'[long_string]'");

        // Extract function/class signatures (Java/TS/JS/Python style)
        String sigs = s.replaceAll("(?m)^(\\s*)(public|private|protected|def|function|class)\\b.*", "$0");

        // Keep first ~300 and last ~200 chars for context
        String head = s.length() > 300 ? s.substring(0, 300) : s;
        String tail = s.length() > 200 ? s.substring(s.length() - 200) : "";

        String combined = sigs + "\n" + head + "\n...\n" + tail;

        // Enforce max length ~800
        return combined.length() > 800 ? combined.substring(0, 800) : combined;
    }

}

