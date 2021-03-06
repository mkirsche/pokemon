package mapMaker.model;

import draw.DrawUtils;
import draw.TileUtils;
import main.Global;
import mapMaker.MapMaker;
import util.FileIO;
import util.FileName;
import util.Folder;
import util.StringUtils;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class TileModel extends MapMakerModel {
    private static final int BLANK_TILE_INDEX = Color.MAGENTA.getRGB(); // TODO: No I hate this
    private static final BufferedImage BLANK_TILE_IMAGE = TileUtils.createBlankTile();
    private static final ImageIcon BLANK_TILE_ICON = new ImageIcon(BLANK_TILE_IMAGE, "0"); // TODO: Is 0 still necessary?

    private final Map<MapMaker.TileCategory, DefaultListModel<ImageIcon>> tileListModel;
    private final Map<TileType, Map<Integer, BufferedImage>> tileMap;
    private final Map<Integer, String> indexMap;

    private boolean saved;
    private MapMaker.TileCategory selectedTileCategory;

    public enum TileType {
        MAP(Folder.MAP_TILES, FileName.MAP_TILES_INDEX),
        MAP_MAKER(Folder.MAP_MAKER_TILES, FileName.MAP_MAKER_TILES_INDEX),
        TRAINER(Folder.TRAINER_TILES, FileName.TRAINER_TILES_INDEX);

        private final String tileFolderPath;
        private final String indexFileName;

        TileType(String folderPath, String fileName) {
            this.tileFolderPath = folderPath;
            this.indexFileName = fileName;
        }
    }

    TileModel() {
        super(BLANK_TILE_INDEX);

        this.selectedTileCategory = MapMaker.TileCategory.ALL;

        this.tileListModel = new HashMap<>();
        for (MapMaker.TileCategory tileCategory : MapMaker.TileCategory.values()) {
            this.tileListModel.put(tileCategory, new DefaultListModel<>());
        }

        this.indexMap = new HashMap<>();
        this.saved = true;

        this.tileMap = new EnumMap<>(TileType.class);
        for (TileType tileType : TileType.values()) {
            this.tileMap.put(tileType, new HashMap<>());
        }
    }

    public void setSelectedTileCategory(MapMaker.TileCategory tileCategory) {
        this.selectedTileCategory = tileCategory;
    }

                                        @Override
    public DefaultListModel<ImageIcon> getListModel() {
        return this.tileListModel.get(selectedTileCategory);
    }

    @Override
    public void reload(MapMaker mapMaker) {
        for (TileType tileType : TileType.values()) {
            this.loadTiles(tileType, mapMaker);
        }

        this.indexMap.put(BLANK_TILE_INDEX, "BlankImage");

        for (MapMaker.TileCategory tileCategory : MapMaker.TileCategory.values()) {
            this.tileListModel.get(tileCategory).add(0, BLANK_TILE_ICON);
        }
    }

    @Override
    public boolean newTileButtonEnabled() {
        return true;
    }

    private Map<Integer, BufferedImage> loadTiles(TileType tileType, MapMaker mapMaker) {
        File indexFile = new File(mapMaker.getPathWithRoot(tileType.indexFileName));
        final Map<Integer, BufferedImage> tileMap = this.tileMap.get(tileType);
        tileMap.clear();

        if (tileType == TileType.MAP) {
            this.indexMap.clear();
            this.tileListModel.clear();
            for (MapMaker.TileCategory tileCategory : MapMaker.TileCategory.values()) {
                this.tileListModel.put(tileCategory, new DefaultListModel<>());
            }
        }

        if (indexFile.exists()) {
            Scanner in = FileIO.openFile(indexFile);
            while (in.hasNext()) {
                String name = in.next();
                int val = (int)Long.parseLong(in.next(), 16);

                MapMaker.TileCategory tileCategory = MapMaker.TileCategory.ALL;
                if (tileType == TileType.MAP) {
                    tileCategory = MapMaker.TileCategory.valueOf(in.next());
                }

                File imageFile = new File(tileType.tileFolderPath + name + ".png");
                if (!imageFile.exists()) {
//                    System.err.println("Could not find image " + imageFile.getName());
                    continue;
                }

                BufferedImage image = FileIO.readImage(imageFile);
                if (tileType == TileType.MAP) {
                    BufferedImage resizedImage = image.getSubimage(
                            0,
                            0,
                            Math.min(image.getWidth(), Global.TILE_SIZE*3),
                            Math.min(image.getHeight(), Global.TILE_SIZE*3)
                    );

                    this.indexMap.put(val, name);
                    ImageIcon imageIcon = new ImageIcon(resizedImage, val + "");
                    this.tileListModel.get(tileCategory).addElement(imageIcon);
                    if (tileCategory != MapMaker.TileCategory.ALL) {
                        this.tileListModel.get(MapMaker.TileCategory.ALL).addElement(imageIcon);
                    }
                }

                tileMap.put(val, image);
            }

            in.close();
        }

        return tileMap;
    }

    public boolean containsTile(TileType tileType, int imageIndex) {
        return this.tileMap.get(tileType).containsKey(imageIndex);
    }

    public BufferedImage getTile(TileType tileType, int index) {
        return this.tileMap.get(tileType).get(index);
    }

    @Override
    public void newTileButtonPressed(MapMaker mapMaker) {

        JFileChooser fileChooser = FileIO.getImageFileChooser(mapMaker.getPathWithRoot(Folder.MAP_TILES));

        int val = fileChooser.showOpenDialog(mapMaker);
        if (val == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File imageFile: files) {
                Color color = JColorChooser.showDialog(mapMaker, "Choose a preferred color for tile: " + imageFile.getName(), Color.WHITE);
                if (color == null) {
                    continue;
                }

                color = DrawUtils.permuteColor(color, indexMap);
                BufferedImage img = FileIO.readImage(imageFile);
                this.tileMap.get(TileType.MAP).put(color.getRGB(), img);
                indexMap.put(color.getRGB(), imageFile.getName());

                tileListModel.get(selectedTileCategory).addElement(new ImageIcon(img, color.getRGB() + ""));
                saved = false;
            }
        }
    }

    public void save(MapMaker mapMaker) {
        if (saved) {
            return;
        }

        saved = true;

        final StringBuilder indexFile = new StringBuilder();
        for (final Entry<Integer, String> entry : indexMap.entrySet()) {
            final String imageIndex = Integer.toString(entry.getKey(), 16);
            final String imageName = entry.getValue();

            StringUtils.appendLine(indexFile, imageName + " " + imageIndex);
        }

        FileIO.writeToFile(mapMaker.getPathWithRoot(FileName.MAP_TILES_INDEX), indexFile);
    }
}
