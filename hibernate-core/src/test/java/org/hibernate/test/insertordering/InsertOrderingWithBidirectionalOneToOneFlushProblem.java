/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.insertordering;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.SEQUENCE;
import static org.hibernate.cfg.AvailableSettings.ORDER_INSERTS;
import static org.hibernate.cfg.AvailableSettings.STATEMENT_BATCH_SIZE;
import static org.hibernate.testing.transaction.TransactionUtil.doInHibernate;

@TestForIssue(jiraKey = "HHH-12105")
public class InsertOrderingWithBidirectionalOneToOneFlushProblem
		extends BaseNonConfigCoreFunctionalTestCase {

	@Test
	public void testInsertSortingWithFlush() {
		doInHibernate(
				this::sessionFactory,
				session -> {
					Flow flow1 = new Flow();
					Formal formal1 = new Formal();
					Pipeline pipeline1 = new Pipeline();
					Project project1 = new Project();
					Project project2 = new Project();
					AclEntry aclEntry = new AclEntry();

					flow1.acl = new Acl();
					flow1.project = project1;
					flow1.sheet = new Sheet();
					flow1.sheet.acl = new Acl();

					formal1.acl = new Acl();

					pipeline1.acl = new Acl();
					pipeline1.formals.add(formal1);
					pipeline1.project = project1;
					pipeline1.sheet = new Sheet();
					pipeline1.sheet.acl = new Acl();

					project1.acl = new Acl();
					project1.sheet = new Sheet();
					project1.sheet.acl = new Acl();

					project2.acl = new Acl();
					project2.acl.addAclEntry(aclEntry);
					project2.sheet = new Sheet();
					project2.sheet.acl = new Acl();

					session.persist(project1);
					session.flush();

					project1.flows.add( flow1 );
					project1.pipelines.add( pipeline1 );

					session.persist(flow1);

					//
					flow1.pipeline = pipeline1;
					pipeline1.flow = flow1;

					session.persist(pipeline1);
					session.persist(formal1);
					session.persist(project2);
					
					session.persist(project2);
				}
		);
	}

	@Override
	protected void addSettings(Map settings) {
		settings.put( ORDER_INSERTS, "false" );
		settings.put( STATEMENT_BATCH_SIZE, "10" );
	}

	@Override
	protected Class[] getAnnotatedClasses() {
		return new Class[]{
				Acl.class,
				AclEntry.class,
				Flow.class,
				Formal.class,
				Pipeline.class,
				Project.class,
				Sheet.class,
		};
	}

	@Entity(name = "Flow")
	public static class Flow {

		@Column(nullable = false)
		@GeneratedValue(
				strategy = SEQUENCE,
				generator = "ID"
		)
		@Id
		@SequenceGenerator(
				name = "ID",
				sequenceName = "FLOW_SEQ"
		)
		private Long id;

		@OneToOne(mappedBy = "flow")
		private Pipeline pipeline;

		@OneToOne(cascade = ALL,
				optional = false)
		private Acl acl;

		@OneToOne(
				cascade = ALL,
				fetch = LAZY,
				orphanRemoval = true
		)
		private Sheet sheet;

		@ManyToOne(optional = false)
		private Project project;
	}
	
	@Entity(name = "AclEntry")
	public static class AclEntry {

		@Column(nullable = false)
		@GeneratedValue(
				strategy = SEQUENCE,
				generator = "ID"
		)
		@Id
		@SequenceGenerator(
				name = "ID",
				sequenceName = "ACL_ENTRY_SEQ"
		)
		private Long           id;

		@ManyToOne(optional = false)
		private Acl acl;
	}

	@Entity(name = "Pipeline")
	public static class Pipeline
	{

		@Column(nullable = false)
		@GeneratedValue(
				strategy = SEQUENCE,
				generator = "ID"
		)
		@Id
		@SequenceGenerator(
				name = "ID",
				sequenceName = "PIPELINE_SEQ"
		)
		private Long id;

		@ManyToOne(optional = false)
		private Project project;

		@JoinColumn
		@OneToOne(
				cascade = ALL,
				fetch = LAZY
		)
		private Flow flow;

		@JoinTable(
				name = "ec_pipeline_formal_param",
				joinColumns =
				@JoinColumn(
						name = "pipeline_id",
						nullable = false,
						updatable = false,
						foreignKey = @ForeignKey(name = "fk_formal_pipeline")
				),
				inverseJoinColumns =
				@JoinColumn(
						name = "formals_id",
						nullable = false,
						updatable = false,
						foreignKey = @ForeignKey(name = "fk_pipeline_formal")
				),
				uniqueConstraints =
				@UniqueConstraint(
						name = "iu_pipeline_formal_param_forma",
						columnNames = "formals_id"
				)
		)
		@OneToMany(
				cascade = ALL,
				fetch = LAZY,
				orphanRemoval = true
		)
		private Set<Formal> formals = new TreeSet<>();

		@OneToOne(cascade = ALL,
				optional = false)
		private Acl acl;

		@OneToOne(
				cascade = ALL,
				fetch = LAZY,
				orphanRemoval = true
		)
		private Sheet sheet;
	}

	@Entity(name = "Acl")
	public static class Acl
	{

		@Column(nullable = false)
		@GeneratedValue(
				strategy = SEQUENCE,
				generator = "ID"
		)
		@Id
		@SequenceGenerator(
				name = "ID",
				sequenceName = "ACL_SEQ"
		)
		private Long id;

		@OneToMany(
				cascade = ALL,
				mappedBy = "acl"
		)
		private List<AclEntry> aclEntries = new ArrayList<>();

		private void addAclEntry(AclEntry aclEntry) {
			aclEntries.add( aclEntry );
			aclEntry.acl = this;
		}
		
	}

	@Entity(name = "Sheet")
	public static class Sheet
	{

		@Column(nullable = false)
		@GeneratedValue(
				strategy = SEQUENCE,
				generator = "ID"
		)
		@Id
		@SequenceGenerator(
				name = "ID",
				sequenceName = "SHEET_SEQ"
		)
		private Long      id;
		
		@OneToOne(cascade = ALL,
				  optional = false)
		private Acl acl;
	}

	@Entity(name = "Formal")
	public static class Formal
		implements Comparable<Formal>
	{
		
		@Override
		public int compareTo(Formal o) {
			return Integer.compare(Objects.hashCode(this), Objects.hashCode(o));
		}

		@Column(nullable = false)
		@GeneratedValue(
				strategy = SEQUENCE,
				generator = "ID"
		)
		@Id
		@SequenceGenerator(
				name = "ID",
				sequenceName = "FORMAL_SEQ"
		)
		private Long id;

		@OneToOne(cascade = ALL,
				  optional = false)
		private Acl      acl;
	}

	@Entity(name = "ProjectEntity")
	public static class Project
	{

		@Column(nullable = false)
		@GeneratedValue(
				strategy = SEQUENCE,
				generator = "ID"
		)
		@Id
		@SequenceGenerator(
				name = "ID",
				sequenceName = "PROJECT_SEQ"
		)
		private Long id;

		@OneToMany(
				fetch = LAZY,
				mappedBy = "project",
				orphanRemoval = true
		)
		private List<Pipeline> pipelines = new ArrayList<>();

		@OneToMany(
				fetch = LAZY,
				mappedBy = "project",
				orphanRemoval = true
		)
		private List<Flow> flows = new ArrayList<>();

		@OneToOne(cascade = ALL,
				  optional = false)
		private Acl acl;

		@OneToOne(
				cascade = ALL,
				fetch = LAZY,
				orphanRemoval = true
		)
		private Sheet sheet;
	}
}
