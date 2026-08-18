package org.dromara.qrcode.domain.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 设备二维码离线识别记录写入请求 (设备协议: 17.4.二维码离线识别记录)
 *
 * @author qrcode
 */
@Data
public class QrRecordWriteBo {

    /**
     * 设备号
     */
    @NotBlank(message = "设备号不能为空")
    private String sn;

    /**
     * 体温 (开启测温才有值)
     */
    @JsonProperty("body_temperature")
    private Double bodyTemperature;

    /**
     * 二维码验证时抓拍的图像
     */
    private String photo;

    /**
     * 二维码id 列表
     */
    @JsonProperty("qrcode_id")
    @NotEmpty(message = "二维码id不能为空")
    private List<Long> qrcodeId;

    /**
     * 访问时间戳列表 (秒级)
     */
    @JsonProperty("create_time")
    private List<Long> createTime;

    /**
     * 全景抓拍照片列表
     */
    @JsonProperty("panoramic_picture")
    private List<String> panoramicPicture;

    /**
     * 健康颜色列表: 1000绿 2000黄 3000红 -1未知
     */
    @JsonProperty("health_code_color")
    private List<String> healthCodeColor;

    /**
     * 健康码抓拍图列表
     */
    @JsonProperty("health_code_picture")
    private List<String> healthCodePicture;

}
