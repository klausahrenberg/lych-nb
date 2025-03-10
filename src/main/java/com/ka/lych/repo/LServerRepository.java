package com.ka.lych.repo;

import com.ka.lych.exception.LException;
import com.ka.lych.util.LFuture;
import java.util.Optional;

/**
 *
 * @author klausahrenberg
 * @param <BC>
 */
public abstract class LServerRepository<BC extends LServerRepository>
        implements ILRepository<BC> {

    @Override
    public <R extends Record> LFuture<R, LException> fetchRoot(Class<R> dataClass, Optional<String> rootName) {
        return LFuture.<R, LException>execute(task -> (R) ROOTS.get(new LRecordClassRootName(this, dataClass, (rootName.isPresent() ? rootName.get() : null))));        
    }

}
