package com.mmall.pojo;

import lombok.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Category {
    private Integer id;

    private Integer parentId;

    private String name;
    //数据库中使用tinyint(1)的话，在使用mybatis-generator工具自动生成代码的时候，这里会自动变成Boolean类型
    private Boolean status;

    private Integer sortOrder;

    private Date createTime;

    private Date updateTime;
}