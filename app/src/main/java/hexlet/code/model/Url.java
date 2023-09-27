package hexlet.code.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.time.Instant;

@Getter
@Setter
@ToString
public final class Url {
    private Long id;
    private String name;
    private Timestamp createdAt;


    public Url() {
    }

    public Url(String name) {
        this.name = name;
    }


    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

   

    public Instant getCreatedAtToInstant() {
        return this.createdAt.toInstant();
    }

    public void setId(Long id) {
        this.id = id;
    }
}

