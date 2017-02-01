package org.innovateuk.ifs.threads.domain;

import org.innovateuk.ifs.user.domain.User;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;

public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private User author;
    private String body;
    private List<PostAttachment> attachments;

    public User author() {
        return author;
    }

    public List<PostAttachment> attachments() {
        return attachments;
    }
}
