package com.tecsup.pe.back_zonet.service.admin;

import com.tecsup.pe.back_zonet.entity.CommunityPost;
import com.tecsup.pe.back_zonet.repository.CommunityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminModerationService {

    @Autowired
    private CommunityRepository communityRepo;

    public List<CommunityPost> listAllPosts() {
        return communityRepo.findAll();
    }

    public void deletePost(Long postId) {
        communityRepo.deleteById(postId);
    }
}