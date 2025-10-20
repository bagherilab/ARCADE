package arcade.potts.env.location;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import arcade.core.util.Vector;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.env.location.Voxel.VOXEL_COMPARATOR;
import static arcade.potts.util.PottsEnums.Direction;
import static arcade.potts.util.PottsEnums.Region;

public class Location3DTest {
    private static final double EPSILON = 1E-10;

    static ArrayList<Voxel> voxelListForDiametersXY;

    static ArrayList<Voxel> voxelListForDiametersYZ;

    static ArrayList<Voxel> voxelListForDiametersZX;

    private static final int[][] VOLUME_SURFACE =
            new int[][] {
                {1, 1, 5},
                {9, 1, 30},
                {18, 2, 43},
                {25, 1, 71},
                {50, 2, 93},
                {75, 3, 115},
                {45, 1, 118},
                {90, 2, 148},
                {135, 3, 177},
                {180, 4, 207},
                {69, 1, 173},
                {138, 2, 210},
                {207, 3, 246},
                {276, 4, 283},
                {345, 5, 319},
                {109, 1, 263},
                {218, 2, 308},
                {327, 3, 354},
                {436, 4, 400},
                {545, 5, 447},
                {654, 6, 493},
                {145, 1, 342},
                {290, 2, 395},
                {435, 3, 448},
                {580, 4, 501},
                {725, 5, 554},
                {870, 6, 607},
                {1015, 7, 660},
                {193, 1, 446},
                {386, 2, 507},
                {579, 3, 568},
                {772, 4, 629},
                {965, 5, 691},
                {1158, 6, 752},
                {1351, 7, 814},
                {1544, 8, 875},
                {249, 1, 566},
                {498, 2, 636},
                {747, 3, 705},
                {996, 4, 775},
                {1245, 5, 845},
                {1494, 6, 914},
                {1743, 7, 984},
                {1992, 8, 1054},
                {2241, 9, 1124},
                {305, 1, 685},
                {610, 2, 762},
                {915, 3, 839},
                {1220, 4, 917},
                {1525, 5, 994},
                {1830, 6, 1071},
                {2135, 7, 1148},
                {2440, 8, 1226},
                {2745, 9, 1303},
                {3050, 10, 1381},
                {373, 1, 830},
                {746, 2, 915},
                {1119, 3, 1000},
                {1492, 4, 1085},
                {1865, 5, 1171},
                {2238, 6, 1256},
                {2611, 7, 1342},
                {2984, 8, 1427},
                {3357, 9, 1513},
                {3730, 10, 1599},
                {4103, 11, 1684},
                {437, 1, 965},
                {874, 2, 1057},
                {1311, 3, 1149},
                {1748, 4, 1241},
                {2185, 5, 1334},
                {2622, 6, 1426},
                {3059, 7, 1519},
                {3496, 8, 1612},
                {3933, 9, 1704},
                {4370, 10, 1797},
                {4807, 11, 1890},
                {5244, 12, 1983},
                {517, 1, 1133},
                {1034, 2, 1233},
                {1551, 3, 1333},
                {2068, 4, 1434},
                {2585, 5, 1534},
                {3102, 6, 1635},
                {3619, 7, 1736},
                {4136, 8, 1837},
                {4653, 9, 1938},
                {5170, 10, 2038},
                {5687, 11, 2139},
                {6204, 12, 2240},
                {6721, 13, 2341},
                {609, 1, 1325},
                {1218, 2, 1434},
                {1827, 3, 1543},
                {2436, 4, 1652},
                {3045, 5, 1761},
                {3654, 6, 1871},
                {4263, 7, 1980},
                {4872, 8, 2090},
                {5481, 9, 2199},
                {6090, 10, 2309},
                {6699, 11, 2418},
                {7308, 12, 2528},
                {7917, 13, 2637},
                {8526, 14, 2747},
                {697, 1, 1509},
                {1394, 2, 1625},
                {2091, 3, 1742},
                {2788, 4, 1859},
                {3485, 5, 1976},
                {4182, 6, 2093},
                {4879, 7, 2210},
                {5576, 8, 2327},
                {6273, 9, 2444},
                {6970, 10, 2561},
                {7667, 11, 2678},
                {8364, 12, 2796},
                {9061, 13, 2913},
                {9758, 14, 3030},
                {10455, 15, 3148},
                {793, 1, 1709},
                {1586, 2, 1833},
                {2379, 3, 1957},
                {3172, 4, 2082},
                {3965, 5, 2207},
                {4758, 6, 2331},
                {5551, 7, 2456},
                {6344, 8, 2581},
                {7137, 9, 2706},
                {7930, 10, 2831},
                {8723, 11, 2956},
                {9516, 12, 3082},
                {10309, 13, 3207},
                {11102, 14, 3332},
                {11895, 15, 3457},
                {12688, 16, 3583},
                {889, 1, 1908},
                {1778, 2, 2039},
                {2667, 3, 2171},
                {3556, 4, 2303},
                {4445, 5, 2435},
                {5334, 6, 2567},
                {6223, 7, 2700},
                {7112, 8, 2832},
                {8001, 9, 2964},
                {8890, 10, 3097},
                {9779, 11, 3229},
                {10668, 12, 3362},
                {11557, 13, 3494},
                {12446, 14, 3627},
                {13335, 15, 3760},
                {14224, 16, 3892},
                {15113, 17, 4025},
                {1005, 1, 2148},
                {2010, 2, 2288},
                {3015, 3, 2428},
                {4020, 4, 2569},
                {5025, 5, 2709},
                {6030, 6, 2850},
                {7035, 7, 2990},
                {8040, 8, 3131},
                {9045, 9, 3272},
                {10050, 10, 3413},
                {11055, 11, 3554},
                {12060, 12, 3694},
                {13065, 13, 3835},
                {14070, 14, 3977},
                {15075, 15, 4118},
                {16080, 16, 4259},
                {17085, 17, 4400},
                {18090, 18, 4541},
                {1125, 1, 2396},
                {2250, 2, 2544},
                {3375, 3, 2693},
                {4500, 4, 2841},
                {5625, 5, 2990},
                {6750, 6, 3139},
                {7875, 7, 3287},
                {9000, 8, 3436},
                {10125, 9, 3585},
                {11250, 10, 3734},
                {12375, 11, 3883},
                {13500, 12, 4033},
                {14625, 13, 4182},
                {15750, 14, 4331},
                {16875, 15, 4480},
                {18000, 16, 4630},
                {19125, 17, 4779},
                {20250, 18, 4929},
                {21375, 19, 5078},
                {1245, 1, 2644},
                {2490, 2, 2800},
                {3735, 3, 2956},
                {4980, 4, 3112},
                {6225, 5, 3268},
                {7470, 6, 3425},
                {8715, 7, 3582},
                {9960, 8, 3738},
                {11205, 9, 3895},
                {12450, 10, 4052},
                {13695, 11, 4209},
                {14940, 12, 4366},
                {16185, 13, 4523},
                {17430, 14, 4680},
                {18675, 15, 4837},
                {19920, 16, 4994},
                {21165, 17, 5151},
                {22410, 18, 5308},
                {23655, 19, 5466},
                {24900, 20, 5623},
                {1369, 1, 2899},
                {2738, 2, 3063},
                {4107, 3, 3227},
                {5476, 4, 3390},
                {6845, 5, 3554},
                {8214, 6, 3719},
                {9583, 7, 3883},
                {10952, 8, 4047},
                {12321, 9, 4212},
                {13690, 10, 4376},
                {15059, 11, 4541},
                {16428, 12, 4705},
                {17797, 13, 4870},
                {19166, 14, 5035},
                {20535, 15, 5199},
                {21904, 16, 5364},
                {23273, 17, 5529},
                {24642, 18, 5694},
                {26011, 19, 5859},
                {27380, 20, 6024},
                {28749, 21, 6189},
                {1513, 1, 3196},
                {3026, 2, 3368},
                {4539, 3, 3540},
                {6052, 4, 3712},
                {7565, 5, 3885},
                {9078, 6, 4057},
                {10591, 7, 4230},
                {12104, 8, 4403},
                {13617, 9, 4576},
                {15130, 10, 4748},
                {16643, 11, 4922},
                {18156, 12, 5095},
                {19669, 13, 5268},
                {21182, 14, 5441},
                {22695, 15, 5614},
                {24208, 16, 5787},
                {25721, 17, 5961},
                {27234, 18, 6134},
                {28747, 19, 6308},
                {30260, 20, 6481},
                {31773, 21, 6654},
                {33286, 22, 6828},
                {1649, 1, 3475},
                {3298, 2, 3655},
                {4947, 3, 3834},
                {6596, 4, 4014},
                {8245, 5, 4194},
                {9894, 6, 4375},
                {11543, 7, 4555},
                {13192, 8, 4735},
                {14841, 9, 4916},
                {16490, 10, 5097},
                {18139, 11, 5277},
                {19788, 12, 5458},
                {21437, 13, 5639},
                {23086, 14, 5820},
                {24735, 15, 6000},
                {26384, 16, 6181},
                {28033, 17, 6362},
                {29682, 18, 6543},
                {31331, 19, 6724},
                {32980, 20, 6906},
                {34629, 21, 7087},
                {36278, 22, 7268},
                {37927, 23, 7449},
                {1789, 1, 3763},
                {3578, 2, 3950},
                {5367, 3, 4137},
                {7156, 4, 4324},
                {8945, 5, 4512},
                {10734, 6, 4700},
                {12523, 7, 4888},
                {14312, 8, 5076},
                {16101, 9, 5264},
                {17890, 10, 5452},
                {19679, 11, 5640},
                {21468, 12, 5828},
                {23257, 13, 6017},
                {25046, 14, 6205},
                {26835, 15, 6393},
                {28624, 16, 6582},
                {30413, 17, 6770},
                {32202, 18, 6959},
                {33991, 19, 7148},
                {35780, 20, 7336},
                {37569, 21, 7525},
                {39358, 22, 7714},
                {41147, 23, 7902},
                {42936, 24, 8091},
                {1941, 1, 4075},
                {3882, 2, 4269},
                {5823, 3, 4464},
                {7764, 4, 4660},
                {9705, 5, 4855},
                {11646, 6, 5051},
                {13587, 7, 5246},
                {15528, 8, 5442},
                {17469, 9, 5638},
                {19410, 10, 5834},
                {21351, 11, 6030},
                {23292, 12, 6226},
                {25233, 13, 6422},
                {27174, 14, 6619},
                {29115, 15, 6815},
                {31056, 16, 7011},
                {32997, 17, 7208},
                {34938, 18, 7404},
                {36879, 19, 7601},
                {38820, 20, 7797},
                {40761, 21, 7994},
                {42702, 22, 8190},
                {44643, 23, 8387},
                {46584, 24, 8584},
                {48525, 25, 8780},
                {2109, 1, 4419},
                {4218, 2, 4622},
                {6327, 3, 4825},
                {8436, 4, 5029},
                {10545, 5, 5232},
                {12654, 6, 5436},
                {14763, 7, 5640},
                {16872, 8, 5845},
                {18981, 9, 6049},
                {21090, 10, 6253},
                {23199, 11, 6458},
                {25308, 12, 6662},
                {27417, 13, 6867},
                {29526, 14, 7071},
                {31635, 15, 7276},
                {33744, 16, 7481},
                {35853, 17, 7685},
                {37962, 18, 7890},
                {40071, 19, 8095},
                {42180, 20, 8300},
                {44289, 21, 8505},
                {46398, 22, 8710},
                {48507, 23, 8915},
                {50616, 24, 9120},
                {52725, 25, 9325},
                {54834, 26, 9530},
                {2285, 1, 4779},
                {4570, 2, 4990},
                {6855, 3, 5202},
                {9140, 4, 5414},
                {11425, 5, 5626},
                {13710, 6, 5838},
                {15995, 7, 6051},
                {18280, 8, 6263},
                {20565, 9, 6476},
                {22850, 10, 6689},
                {25135, 11, 6901},
                {27420, 12, 7114},
                {29705, 13, 7327},
                {31990, 14, 7540},
                {34275, 15, 7753},
                {36560, 16, 7966},
                {38845, 17, 8180},
                {41130, 18, 8393},
                {43415, 19, 8606},
                {45700, 20, 8819},
                {47985, 21, 9033},
                {50270, 22, 9246},
                {52555, 23, 9460},
                {54840, 24, 9673},
                {57125, 25, 9887},
                {59410, 26, 10100},
                {61695, 27, 10314},
                {2449, 1, 5115},
                {4898, 2, 5333},
                {7347, 3, 5552},
                {9796, 4, 5772},
                {12245, 5, 5992},
                {14694, 6, 6211},
                {17143, 7, 6431},
                {19592, 8, 6651},
                {22041, 9, 6871},
                {24490, 10, 7092},
                {26939, 11, 7312},
                {29388, 12, 7532},
                {31837, 13, 7753},
                {34286, 14, 7973},
                {36735, 15, 8194},
                {39184, 16, 8415},
                {41633, 17, 8635},
                {44082, 18, 8856},
                {46531, 19, 9077},
                {48980, 20, 9298},
                {51429, 21, 9519},
                {53878, 22, 9740},
                {56327, 23, 9961},
                {58776, 24, 10182},
                {61225, 25, 10403},
                {63674, 26, 10624},
                {66123, 27, 10845},
                {68572, 28, 11066},
                {2617, 1, 5458},
                {5234, 2, 5684},
                {7851, 3, 5911},
                {10468, 4, 6138},
                {13085, 5, 6365},
                {15702, 6, 6592},
                {18319, 7, 6819},
                {20936, 8, 7047},
                {23553, 9, 7274},
                {26170, 10, 7502},
                {28787, 11, 7730},
                {31404, 12, 7958},
                {34021, 13, 8186},
                {36638, 14, 8414},
                {39255, 15, 8642},
                {41872, 16, 8870},
                {44489, 17, 9098},
                {47106, 18, 9326},
                {49723, 19, 9555},
                {52340, 20, 9783},
                {54957, 21, 10011},
                {57574, 22, 10240},
                {60191, 23, 10468},
                {62808, 24, 10697},
                {65425, 25, 10925},
                {68042, 26, 11154},
                {70659, 27, 11382},
                {73276, 28, 11611},
                {75893, 29, 11840},
                {2809, 1, 5850},
                {5618, 2, 6084},
                {8427, 3, 6319},
                {11236, 4, 6554},
                {14045, 5, 6790},
                {16854, 6, 7025},
                {19663, 7, 7261},
                {22472, 8, 7496},
                {25281, 9, 7732},
                {28090, 10, 7968},
                {30899, 11, 8204},
                {33708, 12, 8440},
                {36517, 13, 8676},
                {39326, 14, 8913},
                {42135, 15, 9149},
                {44944, 16, 9385},
                {47753, 17, 9622},
                {50562, 18, 9858},
                {53371, 19, 10095},
                {56180, 20, 10332},
                {58989, 21, 10568},
                {61798, 22, 10805},
                {64607, 23, 11042},
                {67416, 24, 11278},
                {70225, 25, 11515},
                {73034, 26, 11752},
                {75843, 27, 11989},
                {78652, 28, 12226},
                {81461, 29, 12463},
                {84270, 30, 12700},
            };

    @BeforeAll
    public static void setupLists() {
        int[][] diameter =
                new int[][] {
                    {5, 6, 7, 5, 6, 7, 5, 7, 8, 8, 5, 6, 7, 5, 6, 7},
                    {3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 6, 2, 2, 2},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 2, 2, 2}
                };

        voxelListForDiametersXY = new ArrayList<>();
        voxelListForDiametersYZ = new ArrayList<>();
        voxelListForDiametersZX = new ArrayList<>();

        for (int i = 0; i < diameter[0].length; i++) {
            voxelListForDiametersXY.add(new Voxel(diameter[0][i], diameter[1][i], diameter[2][i]));
            voxelListForDiametersYZ.add(new Voxel(diameter[2][i], diameter[0][i], diameter[1][i]));
            voxelListForDiametersZX.add(new Voxel(diameter[1][i], diameter[2][i], diameter[0][i]));
        }
    }

    @Test
    public void getNeighbors_givenLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(-1, 0, 0));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(0, -1, 0));
        voxels.add(new Voxel(0, 1, 0));
        voxels.add(new Voxel(0, 0, -1));
        voxels.add(new Voxel(0, 0, 1));

        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        ArrayList<Voxel> neighbors = loc.getNeighbors(new Voxel(0, 0, 0));

        voxels.sort(VOXEL_COMPARATOR);
        neighbors.sort(VOXEL_COMPARATOR);

        assertEquals(voxels, neighbors);
    }

    @Test
    public void getNeighbors_givenLocations_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(-1, 0, 0));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(0, -1, 0));
        voxels.add(new Voxel(0, 1, 0));
        voxels.add(new Voxel(0, 0, -1));
        voxels.add(new Voxel(0, 0, 1));

        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        ArrayList<Voxel> neighbors = loc.getNeighbors(new Voxel(0, 0, 0));

        voxels.sort(VOXEL_COMPARATOR);
        neighbors.sort(VOXEL_COMPARATOR);

        assertEquals(voxels, neighbors);
    }

    @Test
    public void getDiameters_validLocationXY_calculatesValues() {
        PottsLocation3D loc = new PottsLocation3D(voxelListForDiametersXY);
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(3, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(2, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(4, (int) diameters.get(Direction.POSITIVE_XY));
        assertEquals(3, (int) diameters.get(Direction.NEGATIVE_XY));
    }

    @Test
    public void getDiameters_invalidLocationXY_returnsZero() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(0, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(0, (int) diameters.get(Direction.POSITIVE_XY));
        assertEquals(0, (int) diameters.get(Direction.NEGATIVE_XY));
    }

    @Test
    public void getDiameters_validLocationYZ_calculatesValues() {
        PottsLocation3D loc = new PottsLocation3D(voxelListForDiametersYZ);
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(3, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(2, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(4, (int) diameters.get(Direction.POSITIVE_YZ));
        assertEquals(3, (int) diameters.get(Direction.NEGATIVE_YZ));
    }

    @Test
    public void getDiameters_invalidLocationYZ_returnsZero() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(0, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(0, (int) diameters.get(Direction.POSITIVE_YZ));
        assertEquals(0, (int) diameters.get(Direction.NEGATIVE_YZ));
    }

    @Test
    public void getDiameters_validLocationZX_calculatesValues() {
        PottsLocation3D loc = new PottsLocation3D(voxelListForDiametersZX);
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(3, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(2, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(4, (int) diameters.get(Direction.POSITIVE_ZX));
        assertEquals(3, (int) diameters.get(Direction.NEGATIVE_ZX));
    }

    @Test
    public void getDiameters_invalidLocationZX_returnsZero() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(0, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(0, (int) diameters.get(Direction.POSITIVE_ZX));
        assertEquals(0, (int) diameters.get(Direction.NEGATIVE_ZX));
    }

    @Test
    public void getDiameters_validLocationsXY_calculatesValues() {
        PottsLocations3D loc = new PottsLocations3D(voxelListForDiametersXY);
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(3, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(2, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(4, (int) diameters.get(Direction.POSITIVE_XY));
        assertEquals(3, (int) diameters.get(Direction.NEGATIVE_XY));
    }

    @Test
    public void getDiameters_invalidLocationsXY_returnsZero() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(0, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(0, (int) diameters.get(Direction.POSITIVE_XY));
        assertEquals(0, (int) diameters.get(Direction.NEGATIVE_XY));
    }

    @Test
    public void getDiameters_validLocationsYZ_calculatesValues() {
        PottsLocations3D loc = new PottsLocations3D(voxelListForDiametersYZ);
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(3, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(2, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(4, (int) diameters.get(Direction.POSITIVE_YZ));
        assertEquals(3, (int) diameters.get(Direction.NEGATIVE_YZ));
    }

    @Test
    public void getDiameters_invalidLocationsYZ_returnsZero() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(0, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(0, (int) diameters.get(Direction.POSITIVE_YZ));
        assertEquals(0, (int) diameters.get(Direction.NEGATIVE_YZ));
    }

    @Test
    public void getDiameters_validLocationsZX_calculatesValues() {
        PottsLocations3D loc = new PottsLocations3D(voxelListForDiametersZX);
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(3, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(2, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(4, (int) diameters.get(Direction.POSITIVE_ZX));
        assertEquals(3, (int) diameters.get(Direction.NEGATIVE_ZX));
    }

    @Test
    public void getDiameters_invalidLocationsZX_returnsZero() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(0, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(0, (int) diameters.get(Direction.POSITIVE_ZX));
        assertEquals(0, (int) diameters.get(Direction.NEGATIVE_ZX));
    }

    @Test
    public void convertSurface_givenLocationValue_calculatesValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        for (int[] sv : VOLUME_SURFACE) {
            assertEquals(sv[2], loc.convertSurface(sv[0], sv[1]), EPSILON);
        }
    }

    @Test
    public void convertSurface_givenLocationsValue_calculatesValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        for (int[] sv : VOLUME_SURFACE) {
            assertEquals(sv[2], loc.convertSurface(sv[0], sv[1]), EPSILON);
        }
    }

    @Test
    public void calculateSurface_emptyList_returnsZero() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        assertEquals(0, loc.calculateSurface());
    }

    @Test
    public void calculateSurface_validVoxels_returnsValue() {
        int[] surfaces = new int[] {6, 10, 14, 18};
        int[][][] voxelLists =
                new int[][][] {
                    {{1, 1, 0}},
                    {{1, 1, 0}, {1, 1, 1}},
                    {{1, 1, 0}, {1, 1, 1}, {1, 2, 0}},
                    {{1, 1, 0}, {1, 1, 1}, {1, 2, 0}, {2, 1, 1}},
                };

        for (int i = 0; i < surfaces.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation3D loc = new PottsLocation3D(voxels);
            assertEquals(surfaces[i], loc.calculateSurface());
        }
    }

    @Test
    public void calculateHeight_emptyList_returnsZero() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        assertEquals(0, loc.calculateHeight());
    }

    @Test
    public void calculateHeight_validVoxels_returnsValue() {
        int[] heights = new int[] {1, 2, 3, 5};
        int[][][] voxelLists =
                new int[][][] {
                    {{1, 1, 0}},
                    {{1, 1, 0}, {1, 1, 1}},
                    {{1, 1, 0}, {1, 1, 1}, {1, 1, 2}},
                    {{1, 1, 0}, {1, 1, 1}, {1, 1, 2}, {1, 1, 4}},
                };

        for (int i = 0; i < heights.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation3D loc = new PottsLocation3D(voxels);
            assertEquals(heights[i], loc.calculateHeight());
        }
    }

    @Test
    public void updateSurface_voxelAdded_returnsValue() {
        int[] surfaces = new int[] {6, 4, 4, 2, 0};
        int[][][] voxelLists =
                new int[][][] {
                    {},
                    {{1, 1, 1}},
                    {{1, 1, 1}, {1, 0, 1}},
                    {{1, 1, 1}, {1, 0, 1}, {1, 0, 0}},
                    {{1, 1, 1}, {1, 0, 1}, {1, 0, 0}, {1, 2, 0}},
                };

        for (int i = 0; i < surfaces.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation3D loc = new PottsLocation3D(voxels);
            assertEquals(surfaces[i], loc.updateSurface(new Voxel(1, 1, 0)));
        }
    }

    @Test
    public void updateSurface_voxelRemoved_returnsValue() {
        int[] surfaces = new int[] {6, 4, 4, 2, 0};
        int[][][] voxelLists =
                new int[][][] {
                    {
                        {1, 1, 0},
                    },
                    {{1, 1, 0}, {1, 1, 1}},
                    {{1, 1, 0}, {1, 1, 1}, {1, 0, 1}},
                    {{1, 1, 0}, {1, 1, 1}, {1, 0, 1}, {1, 0, 0}},
                    {{1, 1, 0}, {1, 1, 1}, {1, 0, 1}, {1, 0, 0}, {1, 2, 0}},
                };

        for (int i = 0; i < surfaces.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation3D loc = new PottsLocation3D(voxels);
            assertEquals(surfaces[i], loc.updateSurface(new Voxel(1, 1, 0)));
        }
    }

    @Test
    public void updateHeight_voxelAdded_returnsValue() {
        int[] heights = new int[] {1, 0, 1, 1, 2, 2, 0, 0, 0, 3};
        int[][][] voxelLists =
                new int[][][] {
                    {},
                    {{0, 0, 2}},
                    {{0, 0, 1}},
                    {{0, 0, 3}},
                    {{0, 0, 0}},
                    {{0, 0, 4}},
                    {{0, 0, 2}, {0, 0, 3}},
                    {{0, 0, 2}, {0, 0, 1}},
                    {{0, 0, 1}, {0, 0, 2}, {0, 0, 3}},
                    {{0, 0, 5}, {0, 0, 6}},
                };

        for (int i = 0; i < heights.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation3D loc = new PottsLocation3D(voxels);
            assertEquals(heights[i], loc.updateHeight(new Voxel(1, 1, 2)));
        }
    }

    @Test
    public void updateHeight_voxelRemoved_returnsValue() {
        int[] heights = new int[] {1, 0, 1, 1, 2, 2, 0, 0, 0, 3};
        int[][][] voxelLists =
                new int[][][] {
                    {{1, 1, 2}},
                    {{1, 1, 2}, {0, 0, 2}},
                    {{1, 1, 2}, {0, 0, 1}},
                    {{1, 1, 2}, {0, 0, 3}},
                    {{1, 1, 2}, {0, 0, 0}},
                    {{1, 1, 2}, {0, 0, 4}},
                    {{1, 1, 2}, {0, 0, 2}, {0, 0, 3}},
                    {{1, 1, 2}, {0, 0, 2}, {0, 0, 1}},
                    {{1, 1, 2}, {0, 0, 1}, {0, 0, 2}, {0, 0, 3}},
                    {{1, 1, 2}, {0, 0, 5}, {0, 0, 6}},
                };

        for (int i = 0; i < heights.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation3D loc = new PottsLocation3D(voxels);
            assertEquals(heights[i], loc.updateHeight(new Voxel(1, 1, 2)));
        }
    }

    @Test
    public void getSlice_givenLocationPlane_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.XY_PLANE, null));
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.YZ_PLANE, null));
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.ZX_PLANE, null));
    }

    @Test
    public void getSlice_givenLocationPositiveXY_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());

        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_XY, 1);
        diametersA.put(Direction.XY_PLANE, 2);

        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.XY_PLANE, 1);
        diametersB.put(Direction.POSITIVE_XY, 2);

        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.POSITIVE_XY, diametersA));
        assertEquals(Direction.NEGATIVE_XY, loc.getSlice(Direction.POSITIVE_XY, diametersB));
    }

    @Test
    public void getSlice_givenLocationNegativeXY_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());

        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_XY, 1);
        diametersA.put(Direction.XY_PLANE, 2);

        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.XY_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_XY, 2);

        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.NEGATIVE_XY, diametersA));
        assertEquals(Direction.POSITIVE_XY, loc.getSlice(Direction.NEGATIVE_XY, diametersB));
    }

    @Test
    public void getSlice_givenLocationPositiveYZ_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());

        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_YZ, 1);
        diametersA.put(Direction.YZ_PLANE, 2);

        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.YZ_PLANE, 1);
        diametersB.put(Direction.POSITIVE_YZ, 2);

        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.POSITIVE_YZ, diametersA));
        assertEquals(Direction.NEGATIVE_YZ, loc.getSlice(Direction.POSITIVE_YZ, diametersB));
    }

    @Test
    public void getSlice_givenLocationNegativeYZ_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());

        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_YZ, 1);
        diametersA.put(Direction.YZ_PLANE, 2);

        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.YZ_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_YZ, 2);

        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.NEGATIVE_YZ, diametersA));
        assertEquals(Direction.POSITIVE_YZ, loc.getSlice(Direction.NEGATIVE_YZ, diametersB));
    }

    @Test
    public void getSlice_givenLocationPositiveZX_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());

        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_ZX, 1);
        diametersA.put(Direction.ZX_PLANE, 2);

        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.ZX_PLANE, 1);
        diametersB.put(Direction.POSITIVE_ZX, 2);

        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.POSITIVE_ZX, diametersA));
        assertEquals(Direction.NEGATIVE_ZX, loc.getSlice(Direction.POSITIVE_ZX, diametersB));
    }

    @Test
    public void getSlice_givenLocationNegativeZX_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());

        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_ZX, 1);
        diametersA.put(Direction.ZX_PLANE, 2);

        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.ZX_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_ZX, 2);

        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.NEGATIVE_ZX, diametersA));
        assertEquals(Direction.POSITIVE_ZX, loc.getSlice(Direction.NEGATIVE_ZX, diametersB));
    }

    @Test
    public void getSlice_givenLocationsPlane_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.XY_PLANE, null));
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.YZ_PLANE, null));
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.ZX_PLANE, null));
    }

    @Test
    public void getSlice_givenLocationsPositiveXY_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());

        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_XY, 1);
        diametersA.put(Direction.XY_PLANE, 2);

        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.XY_PLANE, 1);
        diametersB.put(Direction.POSITIVE_XY, 2);

        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.POSITIVE_XY, diametersA));
        assertEquals(Direction.NEGATIVE_XY, loc.getSlice(Direction.POSITIVE_XY, diametersB));
    }

    @Test
    public void getSlice_givenLocationsNegativeXY_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());

        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_XY, 1);
        diametersA.put(Direction.XY_PLANE, 2);

        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.XY_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_XY, 2);

        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.NEGATIVE_XY, diametersA));
        assertEquals(Direction.POSITIVE_XY, loc.getSlice(Direction.NEGATIVE_XY, diametersB));
    }

    @Test
    public void getSlice_givenLocationsPositiveYZ_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());

        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_YZ, 1);
        diametersA.put(Direction.YZ_PLANE, 2);

        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.YZ_PLANE, 1);
        diametersB.put(Direction.POSITIVE_YZ, 2);

        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.POSITIVE_YZ, diametersA));
        assertEquals(Direction.NEGATIVE_YZ, loc.getSlice(Direction.POSITIVE_YZ, diametersB));
    }

    @Test
    public void getSlice_givenLocationsNegativeYZ_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());

        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_YZ, 1);
        diametersA.put(Direction.YZ_PLANE, 2);

        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.YZ_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_YZ, 2);

        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.NEGATIVE_YZ, diametersA));
        assertEquals(Direction.POSITIVE_YZ, loc.getSlice(Direction.NEGATIVE_YZ, diametersB));
    }

    @Test
    public void getSlice_givenLocationsPositiveZX_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());

        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_ZX, 1);
        diametersA.put(Direction.ZX_PLANE, 2);

        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.ZX_PLANE, 1);
        diametersB.put(Direction.POSITIVE_ZX, 2);

        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.POSITIVE_ZX, diametersA));
        assertEquals(Direction.NEGATIVE_ZX, loc.getSlice(Direction.POSITIVE_ZX, diametersB));
    }

    @Test
    public void getSlice_givenLocationsNegativeZX_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());

        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_ZX, 1);
        diametersA.put(Direction.ZX_PLANE, 2);

        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.ZX_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_ZX, 2);

        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.NEGATIVE_ZX, diametersA));
        assertEquals(Direction.POSITIVE_ZX, loc.getSlice(Direction.NEGATIVE_ZX, diametersB));
    }

    @Test
    public void getSlice_invalidDirection_returnsNull() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        assertNull(loc.getSlice(Direction.UNDEFINED, null));

        PottsLocations3D locs = new PottsLocations3D(new ArrayList<>());
        assertNull(locs.getSlice(Direction.UNDEFINED, null));
    }

    @Test
    public void getSelected_midSizeLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();

        int r = 4;
        int h = 6;
        int n = 10;
        for (int k = 0; k < h; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    voxels.add(new Voxel(i - n / 2, j - n / 2, k - h / 2));
                }
            }
        }

        PottsLocation3D loc = new PottsLocation3D(voxels);
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Math.PI * r * r * h);

        assertTrue(selected.size() < n * n * h);
        for (Voxel voxel : selected) {
            assertTrue(Math.sqrt(Math.pow(voxel.x, 2) + Math.pow(voxel.y, 2)) <= r);
        }
    }

    @Test
    public void getSelected_maxSizeLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();

        int h = 6;
        int n = 10;
        for (int k = 0; k < h; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    voxels.add(new Voxel(i - n / 2, j - n / 2, k - h / 2));
                }
            }
        }

        PottsLocation3D loc = new PottsLocation3D(voxels);
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Integer.MAX_VALUE);

        assertEquals(selected.size(), n * n * h);
    }

    @Test
    public void getSelected_minSizeLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();

        int h = 6;
        int n = 10;
        for (int k = 0; k < h; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    voxels.add(new Voxel(i - n / 2, j - n / 2, k - h / 2));
                }
            }
        }

        PottsLocation3D loc = new PottsLocation3D(voxels);
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), 0);

        assertEquals(selected.size(), 0);
    }

    @Test
    public void getSelected_midSizeLocations_returnsList() {
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();

        int r = 4;
        int h = 6;
        int n = 10;
        for (int k = 0; k < h; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    Voxel voxel = new Voxel(i - n / 2, j - n / 2, k - h / 2);
                    if (i == n / 2 && j == n / 2) {
                        voxelsB.add(voxel);
                    } else if (randomDoubleBetween(0, 1) < 0.5) {
                        voxelsA.add(voxel);
                    } else {
                        voxelsB.add(voxel);
                    }
                }
            }
        }

        PottsLocations3D loc = new PottsLocations3D(voxelsA);
        voxelsB.forEach(voxel -> loc.add(Region.UNDEFINED, voxel.x, voxel.y, voxel.z));
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Math.PI * r * r * h);

        assertTrue(selected.size() < voxelsB.size());
        for (Voxel voxel : selected) {
            assertTrue(Math.sqrt(Math.pow(voxel.x, 2) + Math.pow(voxel.y, 2)) <= r);
            assertTrue(voxelsA.contains(voxel));
            assertFalse(voxelsB.contains(voxel));
        }
    }

    @Test
    public void getSelected_maxSizeLocations_returnsList() {
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();

        int h = 6;
        int n = 10;
        for (int k = 0; k < h; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    Voxel voxel = new Voxel(i - n / 2, j - n / 2, k - h / 2);
                    if (i == n / 2 && j == n / 2) {
                        voxelsB.add(voxel);
                    } else if (randomDoubleBetween(0, 1) < 0.5) {
                        voxelsA.add(voxel);
                    } else {
                        voxelsB.add(voxel);
                    }
                }
            }
        }

        PottsLocations3D loc = new PottsLocations3D(voxelsA);
        voxelsB.forEach(voxel -> loc.add(Region.UNDEFINED, voxel.x, voxel.y, voxel.z));
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Integer.MAX_VALUE);

        assertEquals(selected.size(), voxelsA.size());
    }

    @Test
    public void getSelected_minSizeLocations_returnsList() {
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();

        int h = 6;
        int n = 10;
        for (int k = 0; k < h; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    Voxel voxel = new Voxel(i - n / 2, j - n / 2, k - h / 2);
                    if (i == n / 2 && j == n / 2) {
                        voxelsB.add(voxel);
                    } else if (randomDoubleBetween(0, 1) < 0.5) {
                        voxelsA.add(voxel);
                    } else {
                        voxelsB.add(voxel);
                    }
                }
            }
        }

        PottsLocations3D loc = new PottsLocations3D(voxelsA);
        voxelsB.forEach(voxel -> loc.add(Region.UNDEFINED, voxel.x, voxel.y, voxel.z));
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), 0);

        assertEquals(selected.size(), 0);
    }

    @Test
    public void getVolumeInformedOffsetInApicalFrame2D_returnsExpectedVoxel_atCenter() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        // 3x3 grid centered at (0,0)
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                voxels.add(new Voxel(x, y, 0));
            }
        }
        PottsLocation2D loc = new PottsLocation2D(voxels);

        Vector apicalAxis = new Vector(0, 1, 0); // Y-axis
        ArrayList<Integer> offsets = new ArrayList<>();
        offsets.add(50); // middle of X axis
        offsets.add(50); // middle of Y axis

        Voxel result = loc.getOffsetInApicalFrame2D(offsets, apicalAxis);
        assertEquals(new Voxel(0, 0, 0), result);
    }

    @Test
    public void getVolumeInformedOffsetInApicalFrame2D_returnsExpectedVoxel_upperRight() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        for (int x = 0; x <= 4; x++) {
            for (int y = 0; y <= 4; y++) {
                voxels.add(new Voxel(x, y, 0));
            }
        }
        PottsLocation2D loc = new PottsLocation2D(voxels);

        Vector apicalAxis = new Vector(0, 1, 0); // Y-axis
        ArrayList<Integer> offsets = new ArrayList<>();
        offsets.add(100); // far right of X axis
        offsets.add(100); // top of Y axis

        Voxel result = loc.getOffsetInApicalFrame2D(offsets, apicalAxis);
        assertEquals(new Voxel(4, 4, 0), result);
    }

    @Test
    public void getVolumeInformedOffsetInApicalFrame2D_emptyVoxels_returnsNull() {
        PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());

        Vector apicalAxis = new Vector(1, 0, 0);
        ArrayList<Integer> offsets = new ArrayList<>();
        offsets.add(50);
        offsets.add(50);

        Voxel result = loc.getOffsetInApicalFrame2D(offsets, apicalAxis);
        assertNull(result);
    }

    @Test
    public void getVolumeInformedOffsetInApicalFrame2D_invalidOffset_throwsException() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        PottsLocation2D loc = new PottsLocation2D(voxels);

        Vector apicalAxis = new Vector(1, 0, 0);

        ArrayList<Integer> badOffset = new ArrayList<>();
        badOffset.add(50); // only one element

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    loc.getOffsetInApicalFrame2D(badOffset, apicalAxis);
                });
    }

    @Test
    public void getVolumeInformedOffsetInApicalFrame2D_nonOrthogonalAxis_returnsExpected() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(1, 1, 0));
        voxels.add(new Voxel(2, 2, 0));
        voxels.add(new Voxel(3, 3, 0));
        PottsLocation2D loc = new PottsLocation2D(voxels);

        Vector apicalAxis = new Vector(1, 1, 0); // diagonal
        ArrayList<Integer> offsets = new ArrayList<>();
        offsets.add(0); // lowest orthogonal axis
        offsets.add(100); // farthest along apical

        Voxel result = loc.getOffsetInApicalFrame2D(offsets, apicalAxis);
        assertEquals(new Voxel(3, 3, 0), result);
    }
}
