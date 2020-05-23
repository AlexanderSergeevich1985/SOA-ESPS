package com.soaesps.profile.repository;

import com.soaesps.core.DataModels.device.DeviceInfo;
import com.soaesps.core.Utils.CryptoHelper;
import com.soaesps.profile.config.HibernateConfiguration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {HibernateConfiguration.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class DeviceInfoRepositoryTest {
    @Autowired
    private DeviceInfoRepository deviceInfoRepository;

    @Before
    public void init() {

    }

    @Test
    public void A_load_context_test() {
        Assert.assertNotNull(deviceInfoRepository);
    }

    @Test
    public void B_repository_test() throws Exception {
        final Integer id = deviceInfoRepository.save(getDeviceInfo()).getId();
        final DeviceInfo deviceInfo = deviceInfoRepository.getOne(id);
        Assert.assertNotNull(deviceInfo);
        deviceInfoRepository.delete(deviceInfo);
        Assert.assertNull(deviceInfoRepository.getOne(id));
    }

    protected DeviceInfo getDeviceInfo() throws Exception {
        final DeviceInfo deviceInfo = new DeviceInfo(CryptoHelper.getUuuid());
        deviceInfo.setDeviceType("TestType");
        deviceInfo.setDeviceSoftModel("TestSoft");

        return deviceInfo;
    }
}