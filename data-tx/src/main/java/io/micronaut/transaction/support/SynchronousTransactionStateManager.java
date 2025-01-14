/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.transaction.support;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.TransactionManager;
import io.micronaut.transaction.TransactionState;
import io.micronaut.transaction.TransactionStatus;
import io.micronaut.transaction.exceptions.TransactionException;

/**
 * NOTICE: This is a fork of Spring's {@code PlatformTransactionManager} modernizing it
 * to use enums, Slf4j and decoupling from Spring.
 *
 * This is the central interface in transaction infrastructure.
 * Applications can use this directly, but it is not primarily meant as API:
 * Typically, applications will work with either TransactionTemplate or
 * declarative transaction demarcation through AOP.
 *
 * This version of a transaction manager is using the state passed instead of accessing ThreadLocal values.
 *
 * <p>For implementors, it is recommended to derive from the provided
 * {@link io.micronaut.transaction.support.AbstractSynchronousStateTransactionManager}
 * class, which pre-implements the defined propagation behavior and takes care
 * of transaction synchronization handling. Subclasses have to implement
 * template methods for specific states of the underlying transaction,
 * for example: begin, suspend, resume, commit.
 *
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author graemerocher
 * @author Denis Stepanov
 * @since 16.05.2003
 * @since 3.4.0
 * @param <T> The resource type
 * @param <S> The state type
 */
@Internal
interface SynchronousTransactionStateManager<T, S extends TransactionState> extends TransactionManager, TransactionStateOperations<T, S> {

    /**
     * Return a currently active transaction or create a new one, according to
     * the specified propagation behavior.
     * <p>Note that parameters like isolation level or timeout will only be applied
     * to new transactions, and thus be ignored when participating in active ones.
     * <p>Furthermore, not all transaction definition settings will be supported
     * by every transaction manager: A proper transaction manager implementation
     * should throw an exception when unsupported settings are encountered.
     * <p>An exception to the above rule is the read-only flag, which should be
     * ignored if no explicit read-only mode is supported. Essentially, the
     * read-only flag is just a hint for potential optimization.
     *
     * @param state the transaction state
     * @param definition the TransactionDefinition instance (can be {@code null} for defaults),
     * describing propagation behavior, isolation level, timeout etc.
     * @return transaction status object representing the new or current transaction
     * @throws io.micronaut.transaction.exceptions.TransactionException in case of lookup, creation, or system errors
     * @throws io.micronaut.transaction.exceptions.IllegalTransactionStateException if the given transaction definition
     * cannot be executed (for example, if a currently active transaction is in
     * conflict with the specified propagation behavior)
     * @see TransactionDefinition#getPropagationBehavior
     * @see TransactionDefinition#getIsolationLevel
     * @see TransactionDefinition#getTimeout
     * @see TransactionDefinition#isReadOnly
     */
    @NonNull
    TransactionStatus<T> getTransaction(@NonNull S state, @Nullable TransactionDefinition definition)
            throws TransactionException;

    /**
     * Commit the given transaction, with regard to its status. If the transaction
     * has been marked rollback-only programmatically, perform a rollback.
     * <p>If the transaction wasn't a new one, omit the commit for proper
     * participation in the surrounding transaction. If a previous transaction
     * has been suspended to be able to create a new one, resume the previous
     * transaction after committing the new one.
     * <p>Note that when the commit call completes, no matter if normally or
     * throwing an exception, the transaction must be fully completed and
     * cleaned up. No rollback call should be expected in such a case.
     * <p>If this method throws an exception other than a TransactionException,
     * then some before-commit error caused the commit attempt to fail. For
     * example, an O/R Mapping tool might have tried to flush changes to the
     * database right before commit, with the resulting DataAccessException
     * causing the transaction to fail. The original exception will be
     * propagated to the caller of this commit method in such a case.
     *
     * @param state the transaction state
     * @param status object returned by the {@code getTransaction} method
     * @throws io.micronaut.transaction.exceptions.UnexpectedRollbackException in case of an unexpected rollback
     * that the transaction coordinator initiated
     * @throws io.micronaut.transaction.exceptions.HeuristicCompletionException in case of a transaction failure
     * caused by a heuristic decision on the side of the transaction coordinator
     * @throws io.micronaut.transaction.exceptions.TransactionSystemException in case of commit or system errors
     * (typically caused by fundamental resource failures)
     * @throws io.micronaut.transaction.exceptions.IllegalTransactionStateException if the given transaction
     * is already completed (that is, committed or rolled back)
     * @throws TransactionException if something goes wrong during commit
     * @see TransactionStatus#setRollbackOnly
     */
    void commit(@NonNull S state, TransactionStatus<T> status) throws TransactionException;

    /**
     * Perform a rollback of the given transaction.
     * <p>If the transaction wasn't a new one, just set it rollback-only for proper
     * participation in the surrounding transaction. If a previous transaction
     * has been suspended to be able to create a new one, resume the previous
     * transaction after rolling back the new one.
     * <p><b>Do not call rollback on a transaction if commit threw an exception.</b>
     * The transaction will already have been completed and cleaned up when commit
     * returns, even in case of a commit exception. Consequently, a rollback call
     * after commit failure will lead to an IllegalTransactionStateException.
     *
     * @param state the transaction state
     * @param status object returned by the {@code getTransaction} method
     * @throws io.micronaut.transaction.exceptions.TransactionSystemException in case of rollback or system errors
     * (typically caused by fundamental resource failures)
     * @throws io.micronaut.transaction.exceptions.IllegalTransactionStateException if the given transaction
     * is already completed (that is, committed or rolled back)
     * @throws TransactionException if something goes wrong during rollback
     */
    void rollback(@NonNull S state, TransactionStatus<T> status) throws TransactionException;

}
