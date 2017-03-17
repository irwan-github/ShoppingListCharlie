package com.mirzairwan.shopping;

import com.mirzairwan.shopping.domain.Picture;
import com.mirzairwan.shopping.domain.PictureMgr;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Mirza Irwan on 29/12/16.
 */
public class PictureMgrTest
{
    @Test
    public void resetToOriginalPicture() throws Exception
    {
        Picture targetPicture1 = new Picture(new File("/home/irwan/tmp1"));
        pictureMgr.setNewPicture(targetPicture1);

        pictureMgr.setNewPicture(new Picture(new File("/home/irwan/tmp2")));

        Picture targetPicture3 = new Picture(new File("/home/irwan/target3"));
        pictureMgr.setNewPicture(targetPicture3);

        assertThat(pictureMgr.getPictureInDb().getFile().getPath(), is("/home/irwan/original"));

        //List<Picture> discardedPictures = pictureMgr.discardCurrentViewedPicture();

//        assertThat(discardedPictures.contains(pictureMgr.getOriginalPicture()), is(true));
//        assertThat(discardedPictures.contains(targetPicture1), is(true));
//        assertThat(discardedPictures.size(), is(3));
//
//        assertThat(pictureMgr.getPictureForViewing().getFile().getPath(), is("/home/irwan/target3"));
//
//        pictureMgr.setViewOriginalPicture();
//        assertThat(pictureMgr.getPictureForViewing().getFile().getPath(), is("/home/irwan/original"));
//        assertThat(discardedPictures.size(), is(3));
//        assertThat(discardedPictures.contains(pictureMgr.getOriginalPicture()), is(false));
//        assertThat(discardedPictures.contains(targetPicture3), is(true));


    }

    @Test
    public void setPictureForViewing() throws Exception
    {
        pictureMgr.setNewPicture(new Picture(new File("/home/irwan/tmp2")));

        assertThat(pictureMgr.getPictureInDb().getFile().getPath(), is("/home/irwan/original"));

//        List<Picture> discardedPictures = pictureMgr.discardCurrentViewedPicture();
//        assertThat(discardedPictures.contains(pictureMgr.getOriginalPicture()), is(true));
    }

    @Test
    public void setPictureForViewing_case2() throws Exception
    {
        Picture targetPicture1 = new Picture(new File("/home/irwan/tmp1"));
        pictureMgr.setNewPicture(targetPicture1);

        pictureMgr.setNewPicture(new Picture(new File("/home/irwan/tmp2")));


        assertThat(pictureMgr.getPictureInDb().getFile().getPath(), is("/home/irwan/original"));

//        List<Picture> discardedPictures = pictureMgr.discardCurrentViewedPicture();
//
//        assertThat(discardedPictures.contains(pictureMgr.getOriginalPicture()), is(true));
//        assertThat(discardedPictures.contains(targetPicture1), is(true));
//        assertThat(discardedPictures.size(), is(2));

        assertThat(pictureMgr.getNewPicture().getFile().getPath(), is("/home/irwan/tmp2"));
    }


    @Test
    public void setPictureForViewing_case3() throws Exception
    {
        Picture targetPicture1 = new Picture(new File("/home/irwan/tmp1"));
        pictureMgr.setNewPicture(targetPicture1);

        pictureMgr.setNewPicture(new Picture(new File("/home/irwan/tmp2")));

        pictureMgr.setNewPicture(new File("/home/irwan/target3"));

        assertThat(pictureMgr.getPictureInDb().getFile().getPath(), is("/home/irwan/original"));

//        List<Picture> discardedPictures = pictureMgr.discardCurrentViewedPicture();
//
//        assertThat(discardedPictures.contains(pictureMgr.getOriginalPicture()), is(true));
//        assertThat(discardedPictures.contains(targetPicture1), is(true));
//        assertThat(discardedPictures.size(), is(3));

        assertThat(pictureMgr.getNewPicture().getFile().getPath(), is("/home/irwan/target3"));
    }


    PictureMgr pictureMgr;
    @Before
    public void setUp() throws Exception
    {
        //pictureMgr = new PictureMgr(new Picture(new File("/home/irwan/original")), -1);

    }

    @Test
    public void doNothing()
    {


    }

}