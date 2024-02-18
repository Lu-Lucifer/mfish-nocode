package cn.com.mfish.common.oauth.api.entity;

import cn.com.mfish.common.core.entity.BaseTreeEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Description: 组织结构表
 * @Author: mfish
 * @date: 2022-09-20
 * @Version: V1.2.0
 */
@Data
@TableName("sso_org")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "sso_org对象", description = "组织结构表")
public class SsoOrg extends BaseTreeEntity<String> {
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("组织ID")
    private String id;
    @ApiModelProperty("租户id")
    private String tenantId;
    @ApiModelProperty(value = "组织固定编码(可为空，不允许重复，用来通过此code识别具体是哪个组织)")
    private String orgFixCode;
    @ApiModelProperty(value = "组织编码(自动编码父子关系，不需要传值)")
    private String orgCode;
    @ApiModelProperty(value = "组织级别")
    private Integer orgLevel;
    @ApiModelProperty(value = "组织名称")
    private String orgName;
    @ApiModelProperty(value = "排序")
    private Integer orgSort;
    @ApiModelProperty(value = "负责人")
    private String leader;
    @ApiModelProperty(value = "联系电话")
    private String phone;
    @ApiModelProperty(value = "邮箱")
    private String email;
    @ApiModelProperty(value = "备注")
    private String remark;
    @ApiModelProperty(value = "状态（0正常 1停用）")
    private Integer status;
    @ApiModelProperty(value = "删除标志（0正常 1删除）")
    private Integer delFlag;
    @TableField(exist = false)
    @ApiModelProperty("角色ID列表")
    private List<String> roleIds;
}
