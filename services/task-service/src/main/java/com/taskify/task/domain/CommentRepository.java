package com.taskify.task.domain;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access for {@link Comment}.
 */
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /** Number of comments on one task. */
    interface TaskCommentCount {

        /**
         * Returns the task id.
         *
         * @return task id
         */
        UUID getTaskId();

        /**
         * Returns the comment count.
         *
         * @return number of comments
         */
        long getTotal();
    }

    /**
     * Counts comments for several tasks in one query.
     *
     * @param taskIds task ids
     * @return one row per task that has at least one comment
     */
    @Query("select c.taskId as taskId, count(c) as total from Comment c where c.taskId in :taskIds group by c.taskId")
    List<TaskCommentCount> countByTaskIds(@Param("taskIds") Collection<UUID> taskIds);
}
