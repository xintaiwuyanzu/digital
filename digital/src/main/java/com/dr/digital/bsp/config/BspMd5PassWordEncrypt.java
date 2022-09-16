package com.dr.digital.bsp.config;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import com.dr.digital.bsp.service.BspSyncService;
import com.dr.digital.bsp.service.impl.BspSyncServiceImpl;
import com.dr.digital.util.SM4Utils;
import com.dr.framework.core.organise.service.PassWordEncrypt;
import com.dr.framework.sys.service.DefaultPassWordEncrypt;


/**
 * 浪潮密码是md5加密的，这里需要覆盖平台加密策略
 *
 * @author dr
 */
public class BspMd5PassWordEncrypt extends DefaultPassWordEncrypt implements PassWordEncrypt {
    @Autowired
    BspConfig bspConfig;

    public BspMd5PassWordEncrypt(String encoding) {
        super(encoding);
    }

    /**
     * 添加登录账号时加密密码
     *
     * @param password
     * @param salt
     * @param loginType
     * @return
     */
    @Override
    public String encryptAddLogin(String password, String salt, String loginType) {
        if (BspSyncServiceImpl.LOGIN_TYPE_BSP.equals(loginType)) {
            //因为浪潮密码已经是md5加密的了，所以直接返回密码即可
            return password;
        }
        return super.encryptAddLogin(password, salt, loginType);
    }

    /**
     * 账户登录时密码加密
     *
     * @param password
     * @param salt
     * @param loginType
     * @return
     */
    @Override
    public String encryptValidateLogin(String password, String salt, String loginType) {
        if (BspSyncServiceImpl.LOGIN_TYPE_BSP.equals(loginType)) {
            Assert.isTrue(!StringUtils.isEmpty(password), "密码不能为空！");
            //解码密码
            password = decodePassword(password);
            return DigestUtils.md5DigestAsHex(password.getBytes(getEncodingCharset()));
        }
        return super.encryptValidateLogin(password, salt, loginType);
    }

    /**
     * TODO 其他逻辑可能会出问题
     * 重写密码解密方法
     *
     * @param passWord
     * @return
     */
    @Override
    public String decodePassword(String passWord) {
        return decodeStr(passWord);
    }

    /**
     * 福州这边要求登录账户也加密
     *
     * @param loginId
     * @param loginType
     * @param loginSource
     * @return
     */
    @Override
    public String decodeLoginId(String loginId, String loginType, String loginSource) {
        return decodeStr(loginId);
    }

    /**
     * 按照国密4解密字符串
     * 前端传进来的字符串是hex字符串
     *
     * @param source
     * @return
     */
    protected String decodeStr(String source) {
        String key = bspConfig.getSm4Key();
        byte[] keyBytes = key.getBytes(getEncodingCharset());
        try {
            byte[] result = SM4Utils.decrypt_ECB_Padding(keyBytes, Hex.decode(source));
            return new String(result, getEncodingCharset());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("解密登录信息失败！");
        }
    }

}
