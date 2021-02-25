package com.catchiz.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResult {

    @ApiModelProperty("状态码")
    int code;
    @ApiModelProperty("执行信息")
    private String message;
    @ApiModelProperty("返回结果，List类型")
    private Object data;
    @ApiModelProperty("页码信息类")
    private PageBean pageBean;

    public CommonResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public CommonResult(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
