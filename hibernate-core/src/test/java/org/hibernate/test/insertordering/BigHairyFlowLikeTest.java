
// BigHairyFlowLikeTest.java --
//
// BigHairyFlowLikeTest.java is part of ElectricCommander.
//
// Copyright (c) 2005-2017 Electric Cloud, Inc.
// All rights reserved.
//

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.insertordering;

import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;
import org.junit.FixMethodOrder;
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
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

@FixMethodOrder(NAME_ASCENDING)
public class BigHairyFlowLikeTest
		extends BaseNonConfigCoreFunctionalTestCase {

	//~ Methods ----------------------------------------------------------------

	@Test
	public void testInsertSortingWithFlush1() {
		doInHibernate(this::sessionFactory,
				session -> {
					Flow flow1 = new Flow();
					Formal formal1 = new Formal();
					Pipeline pipeline1 = new Pipeline();
					Project project1 = new Project();
					Project project2 = new Project();
					AclEntry aclEntry = new AclEntry();

					flow1.acl = new Acl();
					flow1.project = project1;
					flow1.propertySheet = new PropertySheet();
					flow1.propertySheet.acl = new Acl();
					formal1.acl = new Acl();
					pipeline1.acl = new Acl();

					pipeline1.formals.add(formal1);
					pipeline1.project = project1;
					pipeline1.propertySheet = new PropertySheet();
					pipeline1.propertySheet.acl = new Acl();
					project1.acl = new Acl();
					project1.propertySheet = new PropertySheet();
					project1.propertySheet.acl = new Acl();
					project2.acl = new Acl();

					project2.acl.addAclEntry(aclEntry);
					project2.propertySheet = new PropertySheet();
					project2.propertySheet.acl = new Acl();

					session.persist(project1);
					session.flush();
					project1.flows.add(flow1);
					project1.pipelines.add(pipeline1);
					session.persist(flow1);

					//
					flow1.pipeline = pipeline1;
					pipeline1.flow = flow1;

					session.persist(pipeline1);
					session.persist(formal1);
					session.persist(project2);
					session.persist(project2);
				});
	}

	@Test
	public void testInsertSortingWithFlush2() {
		doInHibernate(this::sessionFactory,
				session -> {
					Application application = new Application();
					Process process1 = new Process();
					Project project1 = new Project();
					Release release = new Release();

					release.acl = new Acl();
					release.propertySheet = new PropertySheet();
					release.propertySheet.acl = new Acl();
					release.project = project1;

					//
					application.acl = new Acl();
					application.project = project1;
					application.propertySheet = new PropertySheet();
					application.propertySheet.acl = new Acl();

					//
					process1.acl = new Acl();
					process1.propertySheet = new PropertySheet();
					process1.propertySheet.acl = new Acl();
					process1.application = application;
					process1.project = project1;

					//
					project1.acl = new Acl();
					project1.propertySheet = new PropertySheet();
					project1.propertySheet.acl = new Acl();

					//
					application.processes.add(process1);
					project1.applications.add(application);
					project1.releases.add(release);

					//
					session.persist(project1);
					session.persist(application);
					session.persist(process1);
					session.persist(release);
				});
	}
	
	@Test
	public void testInsertSortingWithFlush3() {
		doInHibernate(this::sessionFactory,
				session -> {
					Application application = new Application();
					Process process1 = new Process();
					Project project1 = new Project();
					Project project2 = new Project();
					Release release = new Release();

					release.acl = new Acl();
					release.propertySheet = new PropertySheet();
					release.propertySheet.acl = new Acl();
					release.project = project1;

					//
					application.acl = new Acl();
					application.project = project1;
					application.propertySheet = new PropertySheet();
					application.propertySheet.acl = new Acl();

					//
					process1.acl = new Acl();
					process1.propertySheet = new PropertySheet();
					process1.propertySheet.acl = new Acl();
					process1.application = application;
					process1.project = project1;

					//
					project1.acl = new Acl();
					project1.propertySheet = new PropertySheet();
					project1.propertySheet.acl = new Acl();

					//
					application.processes.add(process1);
					project1.applications.add(application);
					project1.releases.add(release);
					
					//
					session.persist(project1);
					session.persist(application);
					session.persist(process1);
					session.persist(release);

					//
					project2.acl = new Acl();
					project2.propertySheet = new PropertySheet();
					project2.propertySheet.acl = new Acl();

					session.persist(project2);
				});
	}

	@Override
	protected void addSettings(Map settings) {
		settings.put(ORDER_INSERTS, "true");
		settings.put(STATEMENT_BATCH_SIZE, "10");
	}

	@Override
	protected Class[] getAnnotatedClasses() {
		return new Class[]{
				Acl.class,
				AclEntry.class,
				Application.class,
				Component.class,
				Flow.class,
				Formal.class,
				Pipeline.class,
				Process.class,
				Project.class,
				PropertySheet.class,
				Release.class,
		};
	}

	//~ Inner Classes ----------------------------------------------------------

	@Entity(name = "Acl")
	public static class Acl {

		//~ Instance fields ----------------------------------------------------

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

		//~ Methods ------------------------------------------------------------

		private void addAclEntry(AclEntry aclEntry) {
			aclEntries.add(aclEntry);
			aclEntry.acl = this;
		}
	}

	@Entity(name = "AclEntry")
	public static class AclEntry {

		//~ Instance fields ----------------------------------------------------

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
		private Long id;
		@ManyToOne(optional = false)
		private Acl acl;
	}

	@Entity(name = "Application")
	public static class Application {

		//~ Instance fields ----------------------------------------------------

		@Column(nullable = false)
		@GeneratedValue(
				strategy = SEQUENCE,
				generator = "ID"
		)
		@Id
		@SequenceGenerator(
				name = "ID",
				sequenceName = "APP_SEQ"
		)
		private Long id;
		@OneToOne(
				cascade = ALL,
				optional = false
		)
		private Acl acl;
		@OneToOne(
				cascade = ALL,
				fetch = LAZY,
				orphanRemoval = true
		)
		private PropertySheet propertySheet;
		@JoinColumn(
				name = "project_id",
				nullable = false
		)
		@ManyToOne(
				fetch = LAZY,
				optional = false
		)
		private Project project;
		@OneToMany(
				cascade = ALL,
				fetch = LAZY,
				mappedBy = "application",
				orphanRemoval = true
		)
		private List<Process> processes = new ArrayList<>();
	}

	@Entity(name = "Component")
	public static class Component {

		//~ Instance fields ----------------------------------------------------

		@Column(nullable = false)
		@GeneratedValue(
				strategy = SEQUENCE,
				generator = "ID"
		)
		@Id
		@SequenceGenerator(
				name = "ID",
				sequenceName = "COMPONENT_SEQ"
		)
		private Long id;
		@OneToOne(
				cascade = ALL,
				optional = false
		)
		private Acl acl;
		@OneToOne(
				cascade = ALL,
				fetch = LAZY,
				orphanRemoval = true
		)
		private PropertySheet propertySheet;
		@ManyToOne(optional = false)
		private Project project;
		@OneToMany(
				cascade = ALL,
				fetch = LAZY,
				mappedBy = "component",
				orphanRemoval = true
		)
		private List<Process> processes = new ArrayList<>();
	}

	@Entity(name = "Flow")
	public static class Flow {

		//~ Instance fields ----------------------------------------------------

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
		@OneToOne(
				cascade = ALL,
				optional = false
		)
		private Acl acl;
		@OneToOne(
				cascade = ALL,
				fetch = LAZY,
				orphanRemoval = true
		)
		private PropertySheet propertySheet;
		@ManyToOne(optional = false)
		private Project project;
		@OneToOne(mappedBy = "flow")
		private Pipeline pipeline;
	}

	@Entity(name = "Formal")
	public static class Formal
			implements Comparable<Formal> {

		//~ Instance fields ----------------------------------------------------

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
		@OneToOne(
				cascade = ALL,
				optional = false
		)
		private Acl acl;

		//~ Methods ------------------------------------------------------------

		@Override
		public int compareTo(Formal o) {
			return Integer.compare(Objects.hashCode(this), Objects.hashCode(o));
		}
	}

	@Entity(name = "Pipeline")
	public static class Pipeline {

		//~ Instance fields ----------------------------------------------------

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
		@OneToOne(
				cascade = ALL,
				optional = false
		)
		private Acl acl;
		@OneToOne(
				cascade = ALL,
				fetch = LAZY,
				orphanRemoval = true
		)
		private PropertySheet propertySheet;
	}

	@Entity(name = "Process")
	public static class Process {

		//~ Instance fields ----------------------------------------------------

		@Column(nullable = false)
		@GeneratedValue(
				strategy = SEQUENCE,
				generator = "ID"
		)
		@Id
		@SequenceGenerator(
				name = "ID",
				sequenceName = "PROCESS_SEQ"
		)
		private Long id;
		@OneToOne(
				cascade = ALL,
				optional = false
		)
		private Acl acl;
		@OneToOne(
				cascade = ALL,
				fetch = LAZY,
				orphanRemoval = true
		)
		private PropertySheet propertySheet;
		@ManyToOne(optional = false)
		private Project project;
		@JoinColumn
		@ManyToOne(fetch = LAZY)
		private Application application;
		@JoinColumn
		@ManyToOne(fetch = LAZY)
		private Component component;
	}

	@Entity(name = "ProjectEntity")
	public static class Project {

		//~ Instance fields ----------------------------------------------------

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
		private List<Application> applications = new ArrayList<>();
		@OneToMany(
				fetch = LAZY,
				mappedBy = "project",
				orphanRemoval = true
		)
		private List<Flow> flows = new ArrayList<>();
		@OneToOne(
				cascade = ALL,
				optional = false
		)
		private Acl acl;
		@OneToOne(
				cascade = ALL,
				fetch = LAZY,
				orphanRemoval = true
		)
		private PropertySheet propertySheet;
		@OneToMany(
				fetch = LAZY,
				mappedBy = "project",
				orphanRemoval = true
		)
		private List<Release> releases = new ArrayList<>();
	}

	@Entity(name = "PropertySheet")
	public static class PropertySheet {

		//~ Instance fields ----------------------------------------------------

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
		private Long id;
		@OneToOne(
				cascade = ALL,
				optional = false
		)
		private Acl acl;
	}

	@Entity(name = "Release")
	public static class Release {

		//~ Instance fields ----------------------------------------------------

		@Column(nullable = false)
		@GeneratedValue(
				strategy = SEQUENCE,
				generator = "ID"
		)
		@Id
		@SequenceGenerator(
				name = "ID",
				sequenceName = "RELEASE_SEQ"
		)
		private Long id;
		@OneToOne(
				cascade = ALL,
				optional = false
		)
		private Acl acl;
		@OneToOne(
				cascade = ALL,
				fetch = LAZY,
				orphanRemoval = true
		)
		private PropertySheet propertySheet;
		@ManyToOne(optional = false)
		private Project project;
	}
}
