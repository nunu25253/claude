package com.buzzanalysis.application.savedanalysis;

import com.buzzanalysis.application.savedanalysis.dto.SaveAnalysisCommand;
import com.buzzanalysis.application.savedanalysis.dto.SavedAnalysisDto;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysis;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 保存済み分析（ブックマーク）ユースケース。
 */
@Service
public class SavedAnalysisApplicationService {

    private final SavedAnalysisRepository savedAnalysisRepository;
    private final PostRepository postRepository;

    public SavedAnalysisApplicationService(SavedAnalysisRepository savedAnalysisRepository, PostRepository postRepository) {
        this.savedAnalysisRepository = savedAnalysisRepository;
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public List<SavedAnalysisDto> list(UUID userId) {
        return savedAnalysisRepository.findByUserId(userId).stream().map(SavedAnalysisDto::from).toList();
    }

    @Transactional
    public SavedAnalysisDto save(SaveAnalysisCommand command) {
        postRepository.findById(command.postId())
                .orElseThrow(() -> EntityNotFoundException.of("Post", command.postId()));
        SavedAnalysis entity = SavedAnalysis.createNew(command.userId(), command.postId(), command.note());
        return SavedAnalysisDto.from(savedAnalysisRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id, UUID requestingUserId) {
        SavedAnalysis existing = savedAnalysisRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.of("SavedAnalysis", id));
        if (!existing.getUserId().equals(requestingUserId)) {
            throw new BusinessRuleViolationException("You are not allowed to delete this saved analysis");
        }
        savedAnalysisRepository.deleteById(id);
    }
}
