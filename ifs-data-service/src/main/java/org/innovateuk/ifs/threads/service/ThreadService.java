package org.innovateuk.ifs.threads.service;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.finance.domain.ProjectFinance;
import org.innovateuk.ifs.threads.domain.Post;
import org.innovateuk.ifs.threads.domain.Thread;
import org.innovateuk.ifs.threads.repository.ThreadRepository;

import java.util.List;

import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

public class ThreadService<E extends Thread, C> {

    private final ThreadRepository<E> repository;
    private final Class<C> contextClass;

    public ThreadService(ThreadRepository<E> repository, Class<C> contextClassName) {
        this.repository = repository;
        this.contextClass = contextClassName;
    }

    public final ServiceResult<List<E>> findAll(Long contextClassPk) {
        return find(repository.findAllByClassPkAndClassName(contextClassPk, contextClass.getName()),
                notFoundError(contextClass, contextClassPk));
    }

    public final ServiceResult<E> findOne(Long contextClassPk) {
        return find(repository.findByClassPkAndClassName(contextClassPk, ProjectFinance.class.getName()),
                notFoundError(contextClass, contextClassPk));
    }

    public final ServiceResult<Void> create(E E) {
        repository.save(E);
        return serviceSuccess();
    }

    public final ServiceResult<Void> addPost(Post post, Long threadId) {
        return findOne(threadId).andOnSuccessReturn(thread -> {
            thread.addPost(post);
            return repository.save(thread);
        }).andOnSuccessReturnVoid();
    }
}
