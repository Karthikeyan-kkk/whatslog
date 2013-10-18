package com.whatslog.model;

/**
 * Classe base para as entidades do sistema.
 *
 * @author bruno.canto
 */
public abstract class EntidadeAbstrata implements Entidade {

  private static final long serialVersionUID = -6423243627514334176L;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!getClass().isAssignableFrom(obj.getClass())) {
      return false;
    }
    EntidadeAbstrata other = (EntidadeAbstrata) obj;
    if (getId() == null) {
      if (other.getId() != null) {
        return false;
      }
    } else if (!getId().equals(other.getId())) {
      return false;
    }
    return true;
  }

}
