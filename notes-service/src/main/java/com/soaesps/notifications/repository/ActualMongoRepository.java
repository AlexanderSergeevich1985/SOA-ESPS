package com.soaesps.notifications.repository;

import com.soaesps.notifications.domain.MongoHtmlTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Low-level Spring Data MongoDB repository for {@link MongoHtmlTemplate}.
 * Not intended for direct use outside {@link MongoTemplateRepository}.
 *
 * <p>Basic CRUD (findById, existsById, save, deleteById) is inherited
 * from {@link MongoRepository}; this interface adds only projection
 * and search queries that cannot be expressed by derived methods alone.
 */
@Repository
public interface ActualMongoRepository extends MongoRepository<MongoHtmlTemplate, String> {

    /**
     * Returns all template documents with ONLY the _id field populated.
     * The fields projection avoids loading potentially large HTML bodies,
     * keeping listAll() cheap even with thousands of stored templates.
     */
    @Query(value = "{}", fields = "{ '_id' : 1 }")
    List<MongoHtmlTemplate> findAllIds();

    /**
     * Finds templates whose HTML body contains the given raw fragment.
     * Useful for impact analysis — e.g. locating every template that still
     * references an outdated footer, logo URL or legal disclaimer text
     * before a branding or compliance update.
     *
     * @param fragment substring to search for inside htmlContent
     * @return matching templates as full documents
     */
    List<MongoHtmlTemplate> findByHtmlContentContaining(String fragment);

    /**
     * Counts templates whose HTML body contains the given fragment.
     * Cheaper than loading documents when only the number is needed
     * (e.g. "how many templates mention the old support phone?").
     *
     * @param fragment substring to search for inside htmlContent
     * @return number of matching templates
     */
    long countByHtmlContentContaining(String fragment);
}