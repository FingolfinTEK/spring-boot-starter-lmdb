package com.fingolfintek.lmdb.tx;

import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Scope(value = "prototype")
@Inherited
@Transactional(value = "lmdbTransactionManager")
public @interface LMDBTransactional {

	@AliasFor(annotation = Transactional.class)
	boolean readOnly() default false;

	@AliasFor(annotation = Transactional.class)
	Propagation propagation() default Propagation.REQUIRED;
}
