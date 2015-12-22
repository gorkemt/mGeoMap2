package com.wrx.myapplication;

/**
 * Created by gtolan on 12/21/2015.
 */
public class MapLayerField {
    private String _layerName;
    private String _fieldName;
    private String _outputFieldName;

    public MapLayerField(String layerName,String fieldName, String outputFieldName){
        this._fieldName = fieldName;
        this._layerName = layerName;
        this._outputFieldName = outputFieldName;
    }

    public String get_layerName() {
        return _layerName;
    }

    public void set_layerName(String _layerName) {
        this._layerName = _layerName;
    }

    public String get_fieldName() {
        return _fieldName;
    }

    public void set_fieldName(String _fieldName) {
        this._fieldName = _fieldName;
    }

    public String get_outputFieldName() {
        return _outputFieldName;
    }

    public void set_outputFieldName(String _outputFieldName) {
        this._outputFieldName = _outputFieldName;
    }


}
