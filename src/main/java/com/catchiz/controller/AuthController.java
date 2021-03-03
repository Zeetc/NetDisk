package com.catchiz.controller;

import com.catchiz.domain.CommonResult;
import com.catchiz.domain.CommonStatus;
import com.catchiz.domain.User;
import com.catchiz.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService, StringRedisTemplate redisTemplate) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

    private final StringRedisTemplate redisTemplate;

    private final Pattern emailPattern=Pattern.compile("^\\s*\\w+(?:\\.?[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$");

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public CommonResult register(User user) throws SQLIntegrityConstraintViolationException, DataIntegrityViolationException, IOException {
        if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null ||
                user.getUsername().trim().length() < 1 ||
                user.getPassword().trim().length() < 1 ||
                user.getEmail().trim().length() < 1) {
            return new CommonResult(CommonStatus.FORBIDDEN, "账号输入不合法");
        }
        if (userService.checkEmailExist(user.getEmail())) {
            return new CommonResult(CommonStatus.FORBIDDEN, "邮箱已存在");
        }
        if(!emailPattern.matcher(user.getEmail()).matches()){
            return new CommonResult(CommonStatus.FORBIDDEN,"邮箱不合法");
        }
        int userId = userService.register(user);
        if (userId != -1) {
            return new CommonResult(CommonStatus.CREATE, "注册成功", userId);
        } else {
            return new CommonResult(CommonStatus.EXCEPTION, "注册失败");
        }
    }

    @GetMapping("/getVerifyCode")
    @ApiOperation("获取验证码的Authorization")
    public CommonResult getVerifyCode() {
        String uuid = UUID.randomUUID().toString();
        StringBuilder randomCode = new StringBuilder();
        char[] codeSequence = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
                'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        // 随机产生codeCount数字的验证码。
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            // 得到随机产生的验证码数字。
            String strRand = String.valueOf(codeSequence[random.nextInt(36)]);
            // 将产生的四个随机数组合在一起。
            randomCode.append(strRand);
        }
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set(uuid, randomCode.toString(), 1, TimeUnit.MINUTES);
        System.out.println(redisTemplate.opsForValue().get(uuid));
        return new CommonResult(CommonStatus.OK, "获得成功", uuid);
    }

    @GetMapping("/getVerifyPic")
    @ApiOperation("根据提供的Authorization获取验证码图片")
    public void getVerifyPic(HttpServletResponse response,
                             @RequestHeader String Authorization) throws IOException {
        int width = 100;
        int height = 30;
        int codeCount = 4;
        int fontHeight;
        int interLine = 16;
        int codeX;
        int codeY;
        codeX = (width - 4) / (codeCount + 1);
        //height - 10 集中显示验证码
        fontHeight = height - 10;
        codeY = height - 7;

        // 定义图像buffer
        BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D gd = buffImg.createGraphics();
        // 创建一个随机数生成器类
        Random random = new Random();
        // 将图像填充为白色
        gd.setColor(Color.WHITE);
        gd.fillRect(0, 0, width, height);
        // 创建字体，字体的大小应该根据图片的高度来定。
        Font font = new Font("Times New Roman", Font.PLAIN, fontHeight);
        // 设置字体。
        gd.setFont(font);
        // 画边框。
        gd.setColor(Color.BLACK);
        gd.drawRect(0, 0, width - 1, height - 1);
        // 随机产生16条干扰线，使图象中的认证码不易被其它程序探测到。
        gd.setColor(Color.lightGray);
        for (int i = 0; i < interLine; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            gd.drawLine(x, y, x + xl, y + yl);
        }
        // randomCode用于保存随机产生的验证码，以便用户登录后进行验证。
        int red, green, blue;
        // 随机产生codeCount数字的验证码。
        String s = redisTemplate.opsForValue().get(Authorization);
        if (s == null || s.equals("")) s = "Wrong";
        for (int i = 0; i < codeCount; i++) {
            // 得到随机产生的验证码数字。
            String strRand = s.charAt(i) + "";
            // 产生随机的颜色分量来构造颜色值，这样输出的每位数字的颜色值都将不同。
            red = random.nextInt(255);
            green = random.nextInt(255);
            blue = random.nextInt(255);
            // 用随机产生的颜色将验证码绘制到图像中。
            gd.setColor(new Color(red, green, blue));
            gd.drawString(strRand, (i + 1) * codeX, codeY);
            // 将产生的四个随机数组合在一起。
        }
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        // 将图像输出到Servlet输出流中。
        ServletOutputStream sos = response.getOutputStream();
        ImageIO.write(buffImg, "jpeg", sos);
        sos.close();
    }
}
