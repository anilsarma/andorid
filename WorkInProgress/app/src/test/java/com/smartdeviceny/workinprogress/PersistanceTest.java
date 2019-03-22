package com.smartdeviceny.workinprogress;

import com.smartdeviceny.njts.DepartureVisionData;
import com.smartdeviceny.njts.JSONObjectSerializer;
import com.smartdeviceny.njts.Persist;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.Date;

import androidx.annotation.NonNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 24)
public class PersistanceTest {
    @Test
    public void marshallTest() throws Exception {
//        DepartureVisionData departureVisionData = new DepartureVisionData();
//
//
//        departureVisionData.block_id="12333";
//        departureVisionData.createTime = new Date(12);
//        departureVisionData.messages.add("hello");
//        departureVisionData.messages.add("world");


        TestObject to = new TestObject();
        to.ids.add(12);
        to.ids.add(13);
        to.tos.add(new TestObject());
        JSONObject o = JSONObjectSerializer.marshall(to);
        Object obj = JSONObjectSerializer.unmarshall(TestObject.class, o);
        System.out.println(to);
        System.out.println(o);
        System.out.println(obj);
        // the marshelled and unmarsheed values must match
        Assert.assertEquals(to.toString(), obj.toString());


    }
}
