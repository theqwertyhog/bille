package com.bille.models;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class CategoryModel extends QSortFilterProxyModel {

	private CategoryModel(QObject parent) {
		super(parent);
	}

	@Override
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		QModelIndex index0;

		index0 = sourceModel().index(sourceRow, 0, sourceParent);

		QRegExp filter = filterRegExp();
		QAbstractItemModel model = sourceModel();
		boolean matchFound;

		matchFound = filter.indexIn(model.data(index0).toString()) != -1;

		return matchFound;
	}

	@Override
	protected boolean lessThan(QModelIndex left, QModelIndex right) {

		boolean result = false;
		Object leftData = sourceModel().data(left);
		Object rightData = sourceModel().data(right);

		QRegExp emailPattern = new QRegExp("([\\w\\.]*@[\\w\\.]*)");

		String leftString = leftData.toString();
		if (left.column() == 1 && emailPattern.indexIn(leftString) != -1)
			leftString = emailPattern.cap(1);

		String rightString = rightData.toString();
		if (right.column() == 1 && emailPattern.indexIn(rightString) != -1)
			rightString = emailPattern.cap(1);

		result = leftString.compareTo(rightString) < 0;
		
		return result;
	}
}
