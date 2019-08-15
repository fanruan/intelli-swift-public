package com.fr.swift.config.hibernate;

import com.fr.swift.config.oper.ConfigDelete;
import com.fr.swift.config.oper.ConfigWhere;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * @author yee
 * @date 2019-08-04
 */
public class HibernateDelete<T> implements ConfigDelete<T> {
    private Session session;
    private CriteriaDelete<T> delete;
    private CriteriaBuilder builder;
    private Root<T> root;

    public HibernateDelete(Class<T> entityClass, Session session) {
        this.session = session;
        this.builder = this.session.getCriteriaBuilder();
        this.delete = builder.createCriteriaDelete(entityClass);
        this.root = delete.from(entityClass);
    }

    @Override
    public int delete() {
        return session.createQuery(delete).executeUpdate();
    }

    @Override
    public void where(ConfigWhere... wheres) {
        Predicate[] predicates = HibernateConfigUtils.where(builder, wheres, root);
        if (predicates.length > 0) {
            delete.where(predicates);
        }
    }

}
