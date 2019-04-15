package com.jule.domino.base.platform.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * @author
 * @since 2018/11/26 17:07
 */
@Getter
@Setter
public class Balance {
    private String gold;

    private String valid_gold;

    public double getGoldDouble(){
        return Double.parseDouble(gold);
    }

}
