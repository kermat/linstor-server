package com.linbit.linstor.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LsBlkEntry
{
    private String name;
    private long size;
    private boolean rotational;
    private String parentName;
    private String kernelName;
    private String fsType;
    private int major;
    private int minor;
    private String model;
    private String serial;
    private String wwn;

    public enum LsBlkFields {
        NAME("NAME"),
        SIZE("SIZE"),
        PKNAME("PKNAME"),
        KNAME("KNAME"),
        ROTA("ROTA"),
        FSTYPE("FSTYPE"),
        MAJ_MIN("MAJ:MIN"),
        MODEL("MODEL"),
        SERIAL("SERIAL"),
        WWN("WWN");

        private String value;

        LsBlkFields(final String valueRef)
        {
            value = valueRef;
        }

        public static LsBlkFields getEnum(String strVal)
        {
            LsBlkFields ret;
            if (strVal.equals(MAJ_MIN.value))
            {
                ret = MAJ_MIN;
            }
            else
            {
                ret = Enum.valueOf(LsBlkFields.class, strVal);
            }
            return ret;
        }

        @Override
        public String toString()
        {
            return value;
        }
    }

    public LsBlkEntry(List<String> lsblkKeyValues)
    {
        for (final String rawField : lsblkKeyValues)
        {
            final int fieldSep = rawField.indexOf('=');
            final String fieldName = rawField.substring(0, fieldSep);
            final String value = rawField.substring(fieldSep + 2, rawField.length() - 1);

            setField(LsBlkFields.getEnum(fieldName), value);
        }
    }

    public LsBlkEntry(
        String nameRef,
        long sizeRef,
        boolean rotationalRef,
        String parentNameRef,
        String kernelNameRef,
        String fsTypeRef,
        int majorRef,
        int minorRef,
        String modelRef,
        String serialRef,
        String wwnRef
    )
    {
        this.name = nameRef;
        this.size = sizeRef;
        this.rotational = rotationalRef;
        this.parentName = parentNameRef;
        this.kernelName = kernelNameRef;
        this.fsType = fsTypeRef;
        this.major = majorRef;
        this.minor = minorRef;
        this.model = modelRef;
        this.serial = serialRef;
        this.wwn = wwnRef;
    }

    private void setField(LsBlkFields fieldName, String value)
    {
        switch (fieldName)
        {
            case NAME:
                name = value;
                break;
            case SIZE:
                size = Long.parseLong(value);
                break;
            case PKNAME:
                parentName = value;
                break;
            case KNAME:
                kernelName = value;
                break;
            case ROTA:
                rotational = "1".equals(value);
                break;
            case FSTYPE:
                fsType = value;
                break;
            case MAJ_MIN:
                if (value != null && !value.isEmpty())
                {
                    String[] majmin = value.split(":");
                    major = Integer.parseInt(majmin[0]);
                    minor = Integer.parseInt(majmin[1]);
                }
                break;
            case MODEL:
                model = value.trim();
                break;
            case SERIAL:
                serial = value;
                break;
            case WWN:
                wwn = value;
                break;
            default:
                throw new RuntimeException(String.format("Field name '%s' unknown.", fieldName));
        }
    }

    public String getName()
    {
        return name;
    }

    public long getSize()
    {
        return size;
    }

    public boolean isRotational()
    {
        return rotational;
    }

    public String getParentName()
    {
        return parentName;
    }

    public String getFsType()
    {
        return fsType;
    }

    public String getKernelName()
    {
        return kernelName;
    }

    public int getMajor()
    {
        return major;
    }

    public int getMinor()
    {
        return minor;
    }

    public String getModel()
    {
        return model;
    }

    public String getSerial()
    {
        return serial;
    }

    public String getWwn()
    {
        return wwn;
    }

    public Map<String, String> asMap()
    {
        HashMap<String, String> map = new HashMap<>();
        map.put(LsBlkFields.NAME.name(), getName());
        map.put(LsBlkFields.SIZE.name(), getSize() + "");
        map.put(LsBlkFields.ROTA.name(), isRotational() + "");
        map.put(LsBlkFields.FSTYPE.name(), getFsType());
        return map;
    }

    @Override
    public String toString()
    {
        return asMap().toString();
    }
}