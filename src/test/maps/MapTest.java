package test.maps;

import map.AreaData;
import map.MapDataType;
import map.WalkType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import util.FileIO;
import util.Folder;
import util.Point;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapTest {
    private List<TestMap> maps;

    @Before
    public void loadMaps() {
        maps = new ArrayList<>();

        File mapsDirectory = new File(Folder.MAPS);
        for (File mapFolder : FileIO.listDirectories(mapsDirectory)) {
            maps.add(new TestMap(mapFolder));
        }
    }

    @Test
    public void flyLocationTest() {
        for (TestMap map : maps) {
            for (AreaData area : map.getAreas()) {
                if (area.isFlyLocation()) {
                    Assert.assertTrue(
                            "Invalid fly location " + area.getFlyLocation() + " for map " + map.getName(),
                            map.hasEntrance(area.getFlyLocation())
                    );
                }
            }
        }
    }

    @Test
    public void multipleAreaTest() {
        for (TestMap map : maps) {
            AreaData[] areas = map.getAreas();
            Assert.assertTrue(areas.length > 0);

            if (areas.length == 1) {
                continue;
            }

            Dimension dimension = map.getDimension();
            for (int x = 0; x < dimension.width; x++) {
                for (int y = 0; y < dimension.height; y++) {
                    int rgb = map.getRGB(x, y, MapDataType.MOVE);
                    WalkType walkType = WalkType.getWalkType(rgb);
                    if (walkType != WalkType.NOT_WALKABLE) {
                        // Confirm each walkable tile is not in the void area
                        Assert.assertFalse(
                                map.getName() + " " + x + " " + y,
                                map.getArea(new Point(x, y)) == AreaData.VOID);
                    }
                }
            }
        }

    }
}
