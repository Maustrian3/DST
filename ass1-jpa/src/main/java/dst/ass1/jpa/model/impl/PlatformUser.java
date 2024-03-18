package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.IPlatformUser;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;

// TODO chose inheritance pattern mapping (see assignment)
public class PlatformUser implements IPlatformUser {
    @Id
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String tel;

    private Double avgRating;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTel() {
        return tel;
    }

    @Override
    public void setTel(String tel) {
        this.tel = tel;
    }

    @Override
    public Double getAvgRating() {
        return avgRating;
    }

    @Override
    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }
}
