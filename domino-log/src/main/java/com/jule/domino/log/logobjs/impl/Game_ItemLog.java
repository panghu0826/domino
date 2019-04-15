package com.jule.domino.log.logobjs.impl;
import com.jule.domino.log.logobjs.AbstractPlayerLog;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Data
public class Game_ItemLog extends AbstractPlayerLog {
   @Column
   private int itemId;
   @Column(length = 64)
   private String item_name;
   @Column
   private int item_type;
   @Column
   private int num;
}
