package com.fank.f1k2.business.entity;

import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * 题目表
 *
 * @author FanK fan1ke2ke@gmail.com（悲伤的橘子树）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CollectionInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
    * 主键ID
    */
    @TableId(type = IdType.AUTO)
    private Integer id;


    /**
     * 关联的题库ID
     */
    private Integer collectionId;

    /**
     * 题目类型：填空题/单选题/多选题
     */
    private String questionType;

    /**
     * 分值
     */
    private Integer score;

    /**
     * 题目内容/题干
     */
    private String desc;

    /**
     * 创建时间
     */
    private LocalDateTime creationTime;

    /**
     * 最后编辑时间
     */
    private LocalDateTime editTime;

    /**
     * 所属题库
     */
    private String tags;


}
