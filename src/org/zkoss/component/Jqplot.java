package org.zkoss.component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.zkoss.json.JSONObject;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zul.CategoryModel;
import org.zkoss.zul.ChartModel;
import org.zkoss.zul.Div;
import org.zkoss.zul.PieModel;
import org.zkoss.zul.event.ChartDataEvent;
import org.zkoss.zul.event.ChartDataListener;

public class Jqplot extends Div implements IdSpace {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6862660124331454947L;

	// Must
	private ChartModel _model;
	
	private ChartDataListener _dataListener;
	private LinkedList<JSONObject> _seriesList;

	// Optional
	private String _title;
	private String _type;

	// super//
	protected void renderProperties(org.zkoss.zk.ui.sys.ContentRenderer renderer)
			throws java.io.IOException {
		super.renderProperties(renderer);
		
		render(renderer, "type", _type);
		render(renderer, "title", _title);
		render(renderer, "model", getJSONResponse(transferToJSONObject(getModel())));
		
		/**
		 * JSON String Content
		 * "values": "X axis", "Line1":value1, "Line2": value2}
		 * [
		 * 	{"values":"Q1","'2001'":20,"'2002'":40},
		 * 	{"values":"Q2","'2001'":35,"'2002'":60},
		 * 	{"values":"Q3","'2001'":40,"'2002'":70},
		 * 	{"values":"Q4","'2001'":55,"'2002'":90}
		 * ]
		 */
	}
	
	private class DefaultChartDataListener implements ChartDataListener, Serializable {
		private static final long serialVersionUID = 20091125153002L;

		public void onChange(ChartDataEvent event) {
			refresh();
		}
	}
	
	private void refresh() {
		System.out.println("refresh");
		smartUpdate("refresh", getJSONResponse(transferToJSONObject(getModel())));
	}

	public ChartModel getModel() {
		return _model;
	}

	public void setModel(ChartModel model) {
		if (_model != model) {
			if (_model != null)
				_model.removeChartDataListener(_dataListener);
			
			_model = model;
			
			if (_dataListener == null) {
				_dataListener = new DefaultChartDataListener();
				_model.addChartDataListener(_dataListener);
			}
			invalidate(); // Always redraw
		}
	}

	public String getTitle() {
		return _title;
	}

	public void setTitle(String title) {
		this._title = title;
	}

	public String getType() {
		return _type;
	}

	public void setType(String type) {
		this._type = type;
	}
	
	private List<JSONObject> transferToJSONObject(ChartModel model) {
		LinkedList<JSONObject> list = new LinkedList<JSONObject>();
		
		if (model == null || _type == null)
			return list;
		
		if ("pie".equals(_type)) {
			PieModel tempModel = (PieModel) model;
			for (int i = 0; i < tempModel.getCategories().size(); i++) {
				Comparable category = tempModel.getCategory(i);
				JSONObject json = new JSONObject();
				json.put("categoryField", category);
				json.put("dataField", tempModel.getValue(category));
				list.add(json);
			}
		
		} else {
			_seriesList = new LinkedList<JSONObject>();
			CategoryModel tempModel = (CategoryModel) model;
			int seriesLength = tempModel.getSeries().size();
			String fieldKey = isHorizontal(_type) ? "xField" : "yField";
			for (int i = 0; i < seriesLength; i++) {
				Comparable series = tempModel.getSeries(i);
				JSONObject json = new JSONObject();
				json.put(fieldKey, escape(series));
				json.put("displayName", series);
				_seriesList.add(json);
			}
			for (int i = 0; i < tempModel.getCategories().size(); i++) {
				Comparable category = tempModel.getCategory(i);
				JSONObject jData = new JSONObject();
				jData.put("values", category);
				for (int j = 0; j < seriesLength; j++) {
					Comparable series = tempModel.getSeries(j);
					jData.put(escape(series), tempModel.getValue(series, category));
				}
				list.add(jData);
			}
		}
		return list;
	}
	
	// Helper
	
	private static String getJSONResponse(List list) {
		// list may be null.
		if (list == null)
			return "";
		
	    final StringBuffer sb = new StringBuffer().append('[');
	    for (Iterator it = list.iterator(); it.hasNext();) {
	    	String s = String.valueOf(it.next());
            sb.append(s).append(',');
	    }
	    sb.deleteCharAt(sb.length() - 1);
	    sb.append(']');
	    return sb.toString().replaceAll("\\\\", "");
	}
	
	private static final List _VALID_TYPES = Arrays.asList(new Object[] {
		"pie", "line", "bar", "column", "stackbar", "stackcolumn"
	});
	
	private static boolean isValid(String type) {
		return _VALID_TYPES.contains(type);
	}
	
	private static String escape(Object series) {
		return "'" + series + "'";
	}
	
	private static boolean isHorizontal(String type) {
		return "bar".equals(type) || "stackbar".equals(type);
	}	

	/**
	 * The default zclass is "z-jqplot"
	 */
	public String getZclass() {
		return (this._zclass != null ? this._zclass : "z-jqplot");
	}
}