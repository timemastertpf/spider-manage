package com.jin10.spidermanage.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category implements Serializable {


//    private Integer gid;
    private Integer id;

    private String name;

    private long parentId;

    private List<Category> labels;

    private List<Category> children;


}
