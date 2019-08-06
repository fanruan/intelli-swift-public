package com.fr.swift.config.hibernate;

import com.fr.swift.config.oper.ConfigWhere;
import com.fr.swift.config.oper.Expression;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;

/**
 * @author yee
 * @date 2019-08-04
 */
class HibernateConfigUtils {
    static <T> Path<T> getPath(Root<T> root, Expression order) {
        Path<T> path = null;
        String column = order.getColumn();
        String[] properties = column.split("\\.");
        for (String property : properties) {
            if (null == path) {
                path = root.get(property);
            } else {
                path = path.get(property);
            }
        }
        return path;
    }

    static Predicate[] where(CriteriaBuilder builder, ConfigWhere[] wheres, Root root) {
        Predicate[] result = new Predicate[wheres.length];
        for (int i = 0; i < wheres.length; i++) {
            switch (wheres[i].type()) {
                case AND:
                    ConfigWhere[] andWheres = (ConfigWhere[]) wheres[i].getValue();
                    result[i] = builder.and(where(builder, andWheres, root));
                    break;
                case OR:
                    ConfigWhere[] orWheres = (ConfigWhere[]) wheres[i].getValue();
                    result[i] = builder.or(where(builder, orWheres, root));
                    break;
                case LIKE:
                    Path like = getPath(root, wheres[i]);
                    result[i] = builder.like(like, (String) wheres[i].getValue());
                    break;
                case IN:
                    Path in = getPath(root, wheres[i]);
                    result[i] = builder.in(in).getExpression().in((Collection<?>) wheres[i].getValue());
                    break;
                case EQ:
                    Path eq = getPath(root, wheres[i]);
                    result[i] = builder.equal(eq, wheres[i].getValue());
                    break;
                case GT:
                    Path gt = getPath(root, wheres[i]);
                    result[i] = builder.gt(gt, (Number) wheres[i].getValue());
                    break;
                default:
            }
        }
        return result;
    }
}
