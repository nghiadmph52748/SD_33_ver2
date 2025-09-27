package org.example.be_sp.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.response.PagingResponse;
import org.hibernate.annotations.Generated;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.Id;

public class GenericCrudService<E, ID, response, request> {
    private final Class<E> entity;
    private final Class<response> responseClass;
    private final JpaRepository<E, ID> repository;

    public GenericCrudService(Class<E> entity, Class<response> responseClass, Class<request> requestClass,
            JpaRepository<E, ID> repository) {
        this.entity = entity;
        this.responseClass = responseClass;
        this.repository = repository;
    }

    public List<response> getAll() {
        return repository.findAll().stream()
                .map(e -> MapperUtils.map(e, responseClass))
                .collect(Collectors.toList());
    }

    public PagingResponse<response> paging(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new ApiException("Invalid paging parameters", "400");
        }
        return new PagingResponse<>(
                repository.findAll(PageRequest.of(page, size)).map(e -> MapperUtils.map(e, responseClass)), page);
    }

    public response getById(ID id) {
        return MapperUtils.map(repository.findById(id)
                .orElseThrow(() -> new ApiException("Entity with id " + id + " does not exist", "404")), responseClass);
    }

    public E add(request request) {
        try {
            E entity = MapperUtils.map(request, this.entity);
            return repository.save(entity);
        } catch (Exception e) {
            throw new ApiException("đã tồn tại: " + entity, "410");
        }
    }

    public E update(ID id, request request) {
        // Find the existing entity first
        E existingEntity = repository.findById(id)
                .orElseThrow(() -> new ApiException("Entity with id " + id + " does not exist", "404"));

        // Map the request to a new entity
        E updatedEntity = MapperUtils.map(request, this.entity);

        // Set the ID from the existing entity to ensure consistency
        try {
            updatedEntity.getClass().getMethod("setId", id.getClass()).invoke(updatedEntity, id);
        } catch (Exception e) {
            throw new ApiException("Error setting ID for entity: " + this.entity, "500");
        }

        // Preserve all @Generated fields from existing entity
        preserveGeneratedFields(existingEntity, updatedEntity);

        return repository.save(updatedEntity);
    }

    /**
     * Preserve @Generated annotated fields from existing entity to updated entity
     */
    private void preserveGeneratedFields(E existingEntity, E updatedEntity) {
        Field[] fields = this.entity.getDeclaredFields();

        for (Field field : fields) {
            // Skip @Id fields as they're already handled
            if (field.isAnnotationPresent(Id.class)) {
                continue;
            }

            // Check if field is marked as @Generated
            if (field.isAnnotationPresent(Generated.class)) {
                try {
                    // Get getter method name
                    String fieldName = field.getName();
                    String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                    String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

                    // Get the value from existing entity
                    Method getter = this.entity.getMethod(getterName);
                    Object value = getter.invoke(existingEntity);

                    // Set the value to updated entity if it exists
                    if (value != null) {
                        Method setter = this.entity.getMethod(setterName, field.getType());
                        setter.invoke(updatedEntity, value);
                    }
                } catch (Exception e) {
                    // Log warning but continue processing
                    System.err.println("Warning: Could not preserve @Generated field '" + field.getName() + "': "
                            + e.getMessage());
                }
            }
        }
    }
}