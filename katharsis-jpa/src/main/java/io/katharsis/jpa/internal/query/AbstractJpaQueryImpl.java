package io.katharsis.jpa.internal.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.JoinType;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaAttributePath;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.query.backend.JpaQueryBackend;
import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.queryspec.Direction;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterSpec;
import io.katharsis.queryspec.SortSpec;

public abstract class AbstractJpaQueryImpl<T, B extends JpaQueryBackend<?, ?, ?, ?>> implements JpaQuery<T> {

	protected final EntityManager em;
	protected final MetaDataObject meta;
	protected final Class<T> clazz;

	protected JoinType defaultJoinType = JoinType.INNER;
	protected final Map<MetaAttributePath, JoinType> joinTypes = new HashMap<>();

	protected ArrayList<FilterSpec> filterSpecs = new ArrayList<>();
	protected ArrayList<SortSpec> sortSpecs = new ArrayList<>();

	protected boolean autoDistinct = true;
	protected boolean autoGroupBy = false;
	protected boolean distinct = false;
	protected boolean ensureTotalOrder = true;

	protected Class<?> parentEntityClass;
	protected List<?> parentIds;
	protected MetaAttribute parentAttr;

	private VirtualAttributeRegistry virtualAttrs;

	protected AbstractJpaQueryImpl(MetaLookup metaLookup, EntityManager em, Class<T> clazz,
			VirtualAttributeRegistry virtualAttrs) {
		this.em = em;
		this.clazz = clazz;
		this.meta = metaLookup.getMeta(clazz).asDataObject();
		this.virtualAttrs = virtualAttrs;
	}

	@SuppressWarnings("unchecked")
	public AbstractJpaQueryImpl(MetaLookup metaLookup, EntityManager em, Class<?> entityClass,
			VirtualAttributeRegistry virtualAttrs, String attrName, List<?> entityIds) {
		this.em = em;
		this.virtualAttrs = virtualAttrs;

		MetaDataObject parentMeta = metaLookup.getMeta(entityClass).asDataObject();
		MetaAttribute attrMeta = parentMeta.getAttribute(attrName);
		this.meta = attrMeta.getType().asEntity();
		this.clazz = (Class<T>) meta.getImplementationClass();

		this.parentEntityClass = entityClass;
		this.parentAttr = attrMeta;
		this.parentIds = entityIds;
	}

	@Override
	public JpaQuery<T> setEnsureTotalOrder(boolean ensureTotalOrder) {
		this.ensureTotalOrder = ensureTotalOrder;
		return this;
	}

	@Override
	public JpaQuery<T> addFilter(FilterSpec filters) {
		this.filterSpecs.add(filters);
		return this;
	}

	@Override
	public JpaQuery<T> addSortBy(Direction dir, String... path) {
		this.sortSpecs.add(new SortSpec(Arrays.asList(path), dir));
		return this;
	}

	@Override
	public JpaQuery<T> addSortBy(List<String> attributePath, Direction dir) {
		this.sortSpecs.add(new SortSpec(attributePath, dir));
		return this;
	}

	@Override
	public JpaQuery<T> addSortBy(SortSpec order) {
		this.sortSpecs.add(order);
		return this;
	}

	@Override
	public JpaQuery<T> setDefaultJoinType(JoinType joinType) {
		this.defaultJoinType = joinType;
		return this;
	}

	@Override
	public JpaQuery<T> setJoinType(JoinType joinType, String... path) {
		joinTypes.put(meta.resolvePath(Arrays.asList(path)), joinType);
		return this;
	}

	@Override
	public JpaQuery<T> setAutoGroupBy(boolean autoGroupBy) {
		this.autoGroupBy = autoGroupBy;
		return this;
	}

	@Override
	public JpaQuery<T> setDistinct(boolean distinct) {
		this.autoDistinct = false;
		this.distinct = distinct;
		return this;
	}

	@Override
	public JpaQuery<T> addFilter(String attrPath, FilterOperator filterOperator, Object value) {
		return addFilter(Arrays.asList(attrPath.split("\\.")), filterOperator, value);
	}

	@Override
	public JpaQuery<T> addFilter(List<String> attrPath, FilterOperator filterOperator, Object value) {
		addFilter(new FilterSpec(attrPath, filterOperator, value));
		return this;
	}

	public List<SortSpec> getSortSpecs() {
		return sortSpecs;
	}

	public boolean getEnsureTotalOrder() {
		return ensureTotalOrder;
	}

	public JoinType getJoinType(MetaAttributePath path) {
		JoinType joinType = joinTypes.get(path);
		if (joinType == null)
			joinType = defaultJoinType;
		return joinType;
	}

	public VirtualAttributeRegistry getVirtualAttrs() {
		return virtualAttrs;
	}

	public MetaDataObject getMeta() {
		return meta;
	}

	@Override
	public Class<T> getEntityClass() {
		return clazz;
	}

	@Override
	public AbstractQueryExecutorImpl<T> buildExecutor() {
		B backend = newBackend();

		@SuppressWarnings({ "rawtypes", "unchecked" })
		QueryBuilder executorFactory = new QueryBuilder(this, backend);
		executorFactory.applyFilterSpec();
		executorFactory.applySortSpec();
		int numAutoSelections = executorFactory.applyDistinct();

		return newExecutor(backend, numAutoSelections);
	}

	protected abstract AbstractQueryExecutorImpl<T> newExecutor(B ctx, int numAutoSelections);

	protected abstract B newBackend();

	@SuppressWarnings({ "unchecked", "hiding" })
	public <T> List<T> getParentIds() {
		return (List<T>) parentIds;
	}

	public List<FilterSpec> getFilterSpecs() {
		return filterSpecs;
	}

	public MetaAttribute getParentAttr() {
		return parentAttr;
	}
}
