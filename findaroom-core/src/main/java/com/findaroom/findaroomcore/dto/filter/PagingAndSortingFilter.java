package com.findaroom.findaroomcore.dto.filter;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.data.domain.Sort.DEFAULT_DIRECTION;

@Setter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class PagingAndSortingFilter {

    Integer page;
    Integer size;
    List<String> sortBy;
    String direction;

    public Mono<Pageable> getPageable() {
        return Mono
                .zip(getPage(), getSize(), getSort())
                .map(t -> PageRequest.of(t.getT1(), t.getT2(), t.getT3()));
    }

    public Mono<Sort> getSort() {
        return Mono
                .zip(getDirection(), getSortBy(), Sort::by)
                .defaultIfEmpty(Sort.unsorted());
    }

    public Mono<Integer> getPage() {
        return Mono
                .justOrEmpty(page)
                .defaultIfEmpty(0);
    }

    public Mono<Integer> getSize() {
        return Mono
                .justOrEmpty(size)
                .defaultIfEmpty(10);
    }

    public Mono<String[]> getSortBy() {
        return Mono.justOrEmpty(sortBy)
                .map(sort -> sort.toArray(new String[0]));
    }

    public Mono<Sort.Direction> getDirection() {
        return Mono.justOrEmpty(direction)
                .map(Sort.Direction::fromString)
                .defaultIfEmpty(DEFAULT_DIRECTION);
    }
}
