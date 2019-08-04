package com.fr.swift.config.hibernate;

import com.fr.swift.config.oper.ConfigQuery;
import com.fr.swift.config.oper.ConfigWhere;
import com.fr.swift.config.oper.Order;
import com.fr.swift.config.oper.Page;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author yee
 * @date 2018-12-30
 */
public class HibernateQuery<T> implements ConfigQuery<T> {
    private CriteriaQuery<T> criteriaQuery;
    private CriteriaQuery<Long> countQuery;
    private CriteriaBuilder builder;
    private Session session;
    private Root<T> root;
    private Root<T> countRoot;

    public HibernateQuery(Class<T> entity, Session session) {
        this.session = session;
        this.builder = this.session.getCriteriaBuilder();
        this.criteriaQuery = builder.createQuery(entity);
        this.countQuery = builder.createQuery(Long.class);
        this.root = this.criteriaQuery.from(entity);
        this.countRoot = this.countQuery.from(entity);
    }

    @Override
    public List<T> executeQuery() {
        return session.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public void where(ConfigWhere... wheres) {
        Predicate[] predicates = HibernateConfigUtils.where(builder, wheres, root);
        if (predicates.length > 0) {
            criteriaQuery.where(predicates);
            countQuery.where(HibernateConfigUtils.where(builder, wheres, countRoot));
        }
    }

    @Override
    public void orderBy(Order... orders) {
        List<javax.persistence.criteria.Order> list = new ArrayList<javax.persistence.criteria.Order>();
        List<javax.persistence.criteria.Order> count = new ArrayList<javax.persistence.criteria.Order>();
        for (Order order : orders) {
            Path path = HibernateConfigUtils.getPath(root, order);
            Path countPath = HibernateConfigUtils.getPath(countRoot, order);
            if (order.isAsc()) {
                list.add(builder.asc(path));
                count.add(builder.asc(countPath));
            } else {
                list.add(builder.desc(path));
                count.add(builder.desc(countPath));
            }
        }

        if (!list.isEmpty()) {
            criteriaQuery.orderBy(list);
            countQuery.orderBy(count);
        }
    }

    @Override
    public Page<T> executeQuery(int page, int size) {
        countQuery.select(builder.count(countRoot));
        long total = session.createQuery(countQuery).getSingleResult().longValue();
        Page<T> result = new Page<T>();
        result.setCurrentPage(page);
        result.setPageSize(size);
        result.setTotal(total);
        if (total > 0) {
            org.hibernate.query.Query<T> query = session.createQuery(criteriaQuery);
            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);
            result.setData(query.getResultList());
        } else {
            result.setData(Collections.<T>emptyList());
        }
        return result;
    }


}
