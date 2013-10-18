package com.whatslog.model;

import java.io.Serializable;

public interface Entidade extends Serializable {

  /**
   * @return identificador da entidade.
   */
  Serializable getId();

}