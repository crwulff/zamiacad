/*
 * Copyright 2008-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 16, 2005
 */

package org.zamia.analysis.ast;

import java.io.IOException;

import org.zamia.DUManager;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.vhdl.ast.Architecture;
import org.zamia.vhdl.ast.AssociationElement;
import org.zamia.vhdl.ast.AssociationList;
import org.zamia.vhdl.ast.BlockDeclarativeItem;
import org.zamia.vhdl.ast.ComponentDeclaration;
import org.zamia.vhdl.ast.DeclarativeItem;
import org.zamia.vhdl.ast.Entity;
import org.zamia.vhdl.ast.FormalPart;
import org.zamia.vhdl.ast.InstantiatedUnit;
import org.zamia.vhdl.ast.InterfaceDeclaration;
import org.zamia.vhdl.ast.InterfaceList;
import org.zamia.vhdl.ast.Library;
import org.zamia.vhdl.ast.Name;
import org.zamia.vhdl.ast.NameExtension;
import org.zamia.vhdl.ast.NameExtensionSuffix;
import org.zamia.vhdl.ast.OperationName;
import org.zamia.vhdl.ast.VHDLPackage;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ASTDeclarationSearch {

	public final static boolean dump = false;

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private ASTDeclarationSearch() {
	}

	public static DeclarativeItem search(ASTObject aNearest, ZamiaProject aZPrj) throws ZamiaException, IOException {

		DUManager dum = aZPrj.getDUM();

		if (aNearest instanceof NameExtension) {
			aNearest = aNearest.getParent();
			if (dump) {
				logger.debug("SA: nearest is a name extension so i'll use the parent: '%s'", aNearest);
			}
		} else if (aNearest instanceof BlockDeclarativeItem) {
			if (dump) {
				logger.debug("SA: nearest is a block declarative item");
			}
			return (DeclarativeItem) aNearest;
		} else if (aNearest instanceof InterfaceDeclaration) {
			if (dump) {
				logger.debug("SA: nearest is an interface declaration");
			}
			return (DeclarativeItem) aNearest;
		} else if (aNearest instanceof Architecture) {
			if (dump) {
				logger.debug("SA: nearest is an architecture");
			}
			return (DeclarativeItem) aNearest;
		} else if (aNearest instanceof Entity) {
			if (dump) {
				logger.debug("SA: nearest is an entity");
			}
			return (DeclarativeItem) aNearest;
		} else if (aNearest instanceof OperationName) {
			if (dump) {
				logger.debug("SA: nearest is an operation name");
			}
			aNearest = ((OperationName) aNearest).getName();
		}

		ASTObject parent = aNearest.getParent();
		if (parent instanceof FormalPart) {

			if (dump) {
				logger.debug("SA: formal part");
			}

			parent = parent.getParent();

			if (parent instanceof DeclarativeItem) {
				return (DeclarativeItem) parent;
			} else {

				if (parent instanceof AssociationElement) {

					if (dump) {
						logger.debug("SA: parent is an association element: '%s'", parent);
					}

					// probably a component instantiation
					// => show port declaration in corresponding entity/component

					parent = parent.getParent();

					if (parent instanceof AssociationList) {

						parent = parent.getParent();

						if (parent instanceof InstantiatedUnit) {

							InstantiatedUnit iu = (InstantiatedUnit) parent;

							Name name = iu.getName();

							DeclarativeItem decl = search(name, aZPrj);

							if (dump) {
								logger.debug("SA: found instantiated unit '%s' which resolved to '%s'", parent, decl);
							}

							InterfaceList interfaces = null;

							if (decl instanceof ComponentDeclaration) {
								interfaces = ((ComponentDeclaration) decl).getInterfaces();
							} else if (decl instanceof Entity) {
								interfaces = ((Entity) decl).getPorts();
							}

							if (interfaces != null) {

								String id = aNearest.toString();

								if (dump) {
									logger.debug("SA: got interface list, looking for interface '%s'", id);
								}

								int n = interfaces.getNumInterfaces();
								for (int i = 0; i < n; i++) {

									InterfaceDeclaration interf = interfaces.get(i);

									if (interf.getId().equals(id)) {
										return interf;
									}
								}
							}

							InterfaceList generics = null;

							if (decl instanceof ComponentDeclaration) {
								generics = ((ComponentDeclaration) decl).getGenerics();
							} else if (decl instanceof Entity) {
								generics = ((Entity) decl).getGenerics();
							}

							if (generics != null) {

								String id = aNearest.toString();

								if (dump) {
									logger.debug("SA: got generics list, looking for '%s'", id);
								}

								int n = generics.getNumInterfaces();
								for (int i = 0; i < n; i++) {

									InterfaceDeclaration interf = generics.get(i);

									if (interf.getId().equals(id)) {
										return interf;
									}
								}
							}

						}
					}
				} else {
					logger.error("SA: parent is not a DeclarativeItem: '%s'", parent);
					return null;
				}
			}

		}

		DeclarativeItem declaration = null;

		if (aNearest instanceof Name) {

			if (dump) {
				logger.debug("SA: This is an operation name.");
			}

			Name name = (Name) aNearest;

			String id = name.getId();

			declaration = aNearest.findDeclaration(id, aZPrj);

			if (dump) {
				logger.debug("SA: findDeclaration() returned: '%s' ", declaration);
			}

			if (declaration != null && !name.isSimpleName()) {

				if (declaration instanceof Library) {

					if (dump) {
						logger.debug("SA: this is a library");
					}

					Library lib = (Library) declaration;

					NameExtension ext = name.getExtension(0);
					if (ext instanceof NameExtensionSuffix) {
						NameExtensionSuffix nes = (NameExtensionSuffix) ext;

						String sid = nes.getSuffix().getId();

						if (dump) {
							logger.debug("SA: looking for '%s' from NameExtensionSuffix in library '%s'", sid, lib.getId());
						}

						Entity entity = dum.findEntity(lib.getId(), sid);

						if (entity != null) {
							declaration = entity;
							if (dump) {
								logger.debug("SA: This is an entity: '%s'", declaration);
							}
						} else {
							if (dump) {
								logger.debug("SA: not an entity.");
							}
						}

						VHDLPackage pkg = dum.findPackage(lib.getId(), sid);

						if (pkg != null) {
							declaration = pkg;
							if (dump) {
								logger.debug("SA: This is a package: '%s'", declaration);
							}
						} else {
							if (dump) {
								logger.debug("SA: not a package.");
							}
						}
					}
				}
			}
		} else {

			logger.error("SA: Unknown io object: %s", aNearest);

			//			text = text.toUpperCase();
			//			declaration = nearest.findDeclaration(text, zprj, cache);
		}

		if (dump) {
			logger.info("SA: Declaration found: " + declaration);
		}

		return declaration;
	}

}
