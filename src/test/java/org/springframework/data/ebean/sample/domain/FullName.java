package org.springframework.data.ebean.sample.domain;

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Xuegui Yuan
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class FullName implements Serializable {
  private String firstName;
  private String lastName;
}
