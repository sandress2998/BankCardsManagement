package com.example.bankcards.util.specification;

import com.example.bankcards.dto.AdminCardFilter;
import com.example.bankcards.dto.CardFilter;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardUpdateStatusRequest;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;


public final class CardSpecifications {

    private CardSpecifications() {}

    public static Specification<Card> byFilter(AdminCardFilter f) {
        return Specification.<Card>unrestricted()
                .and(hasStatus(f.getStatus()))
                .and(hasOwner(f.getOwnerId()))
                .and(hasStatusUpdateRequest(f.getStatusUpdateRequest()));
    }

    public static Specification<Card> byFilter(CardFilter f) {
        UUID ownerId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());


        return Specification.<Card>unrestricted()
                .and(hasOwner(ownerId))
                .and(hasStatus(f.getStatus()));
    }

    private static Specification<Card> hasStatus(Card.Status status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<Card> hasOwner(UUID ownerId) {
        return (root, query, cb) ->
                ownerId == null ? null : cb.equal(root.get("owner").get("id"), ownerId);
    }

    /**
     * пример, если нужно фильтровать только карты,
     * на которые есть заявки об изменении статуса
     */
    private static Specification<Card> hasStatusUpdateRequest(Card.Status req) {
        return (root, query, cb) -> {
            if (req == null) return null;

            // Подзапрос, проверяющий наличие записи в card_update_status_request
            assert query != null;
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<CardUpdateStatusRequest> subRoot = sub.from(CardUpdateStatusRequest.class);
            sub.select(subRoot.get("card").get("id"))
                    .where(
                            cb.equal(subRoot.get("card").get("id"), root.get("id")),
                            cb.equal(subRoot.get("status"), req)
                    );

            return cb.exists(sub);
        };
    }
}



/*
@Component
public class SpecificationBuilder<T> {

    // Статический метод создания для thread-safety
    public static <T> SpecificationBuilder<T> where() {
        return new SpecificationBuilder<>();
    }

    private Specification<T> specification;

    private SpecificationBuilder() {
        this.specification = Specification.where(null);
    }

    public SpecificationBuilder<T> and(String field, Object value, SearchOperation operation) {
        if (value != null && isValidValue(value, operation)) {
            specification = specification.and(createSpecification(field, value, operation));
        }
        return this;
    }

    public SpecificationBuilder<T> or(String field, Object value, SearchOperation operation) {
        if (value != null && isValidValue(value, operation)) {
            specification = specification.or(createSpecification(field, value, operation));
        }
        return this;
    }

    public Specification<T> build() {
        return specification;
    }

    private Specification<T> createSpecification(String field, Object value, SearchOperation operation) {
        return (root, query, cb) -> {
            try {
                Path<Object> path = getPath(root, field);

                switch (operation) {
                    case EQUAL:
                        return cb.equal(path, value);
                    case NOT_EQUAL:
                        return cb.notEqual(path, value);
                    case LIKE:
                        return cb.like(path.as(String.class), "%" + value + "%");
                    case LIKE_START:
                        return cb.like(path.as(String.class), value + "%");
                    case LIKE_END:
                        return cb.like(path.as(String.class), "%" + value);
                    case GREATER_THAN:
                        return cb.greaterThan(path.as(Comparable.class), (Comparable) value);
                    case GREATER_THAN_OR_EQUAL:
                        return cb.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
                    case LESS_THAN:
                        return cb.lessThan(path.as(Comparable.class), (Comparable) value);
                    case LESS_THAN_OR_EQUAL:
                        return cb.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
                    case IN:
                        if (value instanceof Collection) {
                            return path.in((Collection<?>) value);
                        } else {
                            return path.in(value);
                        }
                    case NOT_IN:
                        if (value instanceof Collection) {
                            return cb.not(path.in((Collection<?>) value));
                        } else {
                            return cb.not(path.in(value));
                        }
                    case IS_NULL:
                        return cb.isNull(path);
                    case IS_NOT_NULL:
                        return cb.isNotNull(path);
                    default:
                        throw new IllegalArgumentException("Неподдерживаемая операция: " + operation);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Ошибка создания спецификации для поля: " + field, e);
            }
        };
    }

    // Поддержка вложенных полей (owner.name, address.city.name)
    private Path<Object> getPath(Root<T> root, String field) {
        String[] fields = field.split("\\.");
        Path<Object> path = root.get(fields);

        for (int i = 1; i < fields.length; i++) {
            path = path.get(fields[i]);
        }

        return path;
    }

    // Валидация значений
    private boolean isValidValue(Object value, SearchOperation operation) {
        if (value == null) return false;

        switch (operation) {
            case LIKE:
            case LIKE_START:
            case LIKE_END:
                return value instanceof String;
            case GREATER_THAN:
            case LESS_THAN:
            case GREATER_THAN_OR_EQUAL:
            case LESS_THAN_OR_EQUAL:
                return value instanceof Comparable;
            case IN:
            case NOT_IN:
                return value instanceof Collection || value.getClass().isArray();
            default:
                return true;
        }
    }
}
*/

