package io.github.hexagonframework.data.ebean.sample.domain;

import java.io.Serializable;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Xuegui Yuan
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class FullName implements Serializable {
  private String firstName;
  private String lastName;
}
