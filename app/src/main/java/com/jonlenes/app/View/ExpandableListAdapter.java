package com.jonlenes.app.View;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.jonlenes.app.Modelo.Local;
import com.jonlenes.app.Modelo.Reserve;
import com.jonlenes.app.R;
import com.jonlenes.app.Util;

import java.util.List;
import java.util.Map;

/**
 * Created by Jonlenes on 18/07/2016.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context _context;
    private List<Local> _listDataHeader;
    private final Map<Local, List<Reserve>> _listDataChild;

    public ExpandableListAdapter(Context context, List<Local> listDataHeader,
                                 Map<Local, List<Reserve>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        Reserve child = (Reserve) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.elv_times_item, null);
        }

        ((TextView) convertView.findViewById(R.id.tvDateReserve)).setText(Util.formatDate(child.getDateDay()));
        ((TextView) convertView.findViewById(R.id.tvTimesStart)).setText(Util.formatTime(child.getStartTime()));
        ((TextView) convertView.findViewById(R.id.tvTimesEnd)).setText(Util.formatTime(child.getEndTime()));
        ((TextView) convertView.findViewById(R.id.tvDuration)).setText(child.getDuration() + "min");


        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        Local group = (Local) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.elv_times_group, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(group.getDescription());

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public Map<Local, List<Reserve>> get_listDataChild() {
        return _listDataChild;
    }
}
