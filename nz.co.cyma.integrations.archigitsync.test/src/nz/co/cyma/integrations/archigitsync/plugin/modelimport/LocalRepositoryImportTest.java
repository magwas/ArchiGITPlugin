package nz.co.cyma.integrations.archigitsync.plugin.modelimport;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;


import org.eclipse.emf.ecore.EObject;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateDiagramModel;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IAssociationRelationship;
import com.archimatetool.model.IBusinessObject;
import com.archimatetool.model.IDiagramModelArchimateObject;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.impl.ArchimateFactory;
import com.archimatetool.model.util.ArchimateModelUtils;

public class LocalRepositoryImportTest {

	public class TestModelBuilder {
		IArchimateModel model = ArchimateFactory.eINSTANCE.createArchimateModel();
		LocalRepositoryImport imp = new LocalRepositoryImport();
		Map<String, Map<String,Object>> objects = new HashMap<String, Map<String,Object>>();
		public void createBO1() {
			objects.clear();
			String objectString = "id: bo1\n" +
					"name: example business object 1\n" +
					"elementType: BusinessObject\n" +
					"folderPath: Whatever\n" +
					"properties: []";
			addObjectFromString(objects, objectString);
			imp.createModelObjects(model, objects);
		}
		public void createBO2() {
			objects.clear();
			String objectString = "id: bo2\n" +
					"name: example business object 2\n" +
					"elementType: BusinessObject\n" +
					"folderPath: Whatever\n" +
					"properties: []";
			addObjectFromString(objects, objectString);
			imp.createModelObjects(model, objects);
		}
		public void createAssociation() {
			createBO1();
			createBO2();
			objects.clear();
			String objectString = "id: assoc1\n" +
					"name: example association relation\n" +
					"elementType: AssociationRelationship\n" +
					"sourceElement: bo1\n" +
					"targetElement: bo2\n" +
					"folderPath: Whatever\n" +
					"type: relations\n" +
					"properties: []";
			addObjectFromString(objects, objectString);
			imp.createModelObjects(model, objects);
		}
		
		public void createDiagram() {
			createAssociation();
			objects.clear();
			String objectString = "id: diag1\n" +
					"name: example diagram\n" +
					"diagramElements:\n   dmo1: {elementObjectId: bo1, fillColour: null, font: null, fontColour: null, " +
					"boundsX: '166', boundsY: '73', boundsWidth: '-1', boundsHeight: '-1', textAlignment: '2', " +
					"textPosition: '0', representationType: '0'}\n" +
					"diagramSpecificElements: {}\n" +
					"diagramFeatures: {connectionRouterType: '0'}\n" +
					"properties: []\n" +
					"folderPath: Whatever\n" +
					"type: diagrams\n" +
					"elementType: ArchimateDiagramModel\n";
			addObjectFromString(objects, objectString);
			imp.createModelObjects(model, objects);
		}
			private Map<String, Object> addObjectFromString(
					Map<String, Map<String, Object>> objects, String objectString) {
				Yaml yaml = new Yaml();
				@SuppressWarnings("unchecked")
				Map<String,Object> obj = (Map<String, Object>) yaml.load(objectString);
				objects.put((String) obj.get("id"), obj);
				return obj;
			}

	}

	@Test
	public void createModelObjectsTest() {
		IArchimateModel model = ArchimateFactory.eINSTANCE.createArchimateModel();
		Map<String,Map<String,Object>> objects = new HashMap<String,Map<String,Object>>();
		LocalRepositoryImport imp = new LocalRepositoryImport();
		imp.createModelObjects(model, objects);
		}
	
	@Test
	public void createBusinessObjectTest() {
		TestModelBuilder mb = new TestModelBuilder();
		
		mb.createBO1();
		
		EObject theobj = ArchimateModelUtils.getObjectByID(mb.model, "bo1");
		
		assertTrue(theobj instanceof IBusinessObject);
		assertEquals(((IBusinessObject) theobj).getName(), "example business object 1");
		IFolder parentFolder = (IFolder) theobj.eContainer();
		assertEquals(parentFolder.getName(), "Whatever");
		assertEquals(((IFolder)parentFolder.eContainer()).getType(),FolderType.BUSINESS);
		}
	
	@Test
	public void createReationshipTest() {
		TestModelBuilder mb = new TestModelBuilder();
		
		mb.createAssociation();
		
		EObject theobj = ArchimateModelUtils.getObjectByID(mb.model, "assoc1");
		assertTrue(theobj instanceof IAssociationRelationship);
		IAssociationRelationship rel = (IAssociationRelationship) theobj;
		assertEquals(rel.getSource().getId(),"bo1");
		assertEquals(rel.getTarget().getId(),"bo2");
		assertEquals(rel.getName(), "example association relation");
		IFolder parentFolder = (IFolder) rel.eContainer();
		assertEquals(parentFolder.getName(), "Whatever");
		assertEquals(((IFolder)parentFolder.eContainer()).getType(),FolderType.RELATIONS);
		}
	@Test
	public void createDiagramTest() {
		TestModelBuilder mb = new TestModelBuilder();
		
		mb.createDiagram();
		
		EObject theobj = ArchimateModelUtils.getObjectByID(mb.model, "diag1");
		System.out.printf("theobj=%s\n", theobj);
		assertTrue(theobj instanceof IArchimateDiagramModel);
		IArchimateDiagramModel diag = (IArchimateDiagramModel) theobj;
		IDiagramModelArchimateObject dmo = (IDiagramModelArchimateObject) diag.getChildren().get(0);
		assertEquals(dmo.getId(),"dmo1");
		assertEquals(dmo.getArchimateElement().getId(),"bo1");
		IFolder parentFolder = (IFolder) diag.eContainer();
		assertEquals(parentFolder.getName(), "Whatever");
		assertEquals(((IFolder)parentFolder.eContainer()).getType(),FolderType.DIAGRAMS);
		}
}
