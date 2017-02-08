package org.innovateuk.ifs.thread.viewmodel;

import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.threads.resource.PostResource;

import java.time.LocalDateTime;
import java.util.List;

public class ThreadPostViewModel extends PostResource {
    private String username;

    public ThreadPostViewModel(Long id, UserResource author, String body, List<FileEntryResource> attachments, LocalDateTime createdOn) {
        super(id, author, body, attachments, createdOn);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
