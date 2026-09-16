package com.example.url_shortener.shorturl;

import com.example.url_shortener.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByShortCode(String shortCode);

    List<ShortUrl> findAllByUser(User user);

    List<ShortUrl> findAllByUserAndExpiresAtAfter(User user, OffsetDateTime now);

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
        update ShortUrl s
        set s.clickCount = s.clickCount + 1
        where s.id = :id
        """)
    int incrementClickCountById(@Param("id") Long id);
}