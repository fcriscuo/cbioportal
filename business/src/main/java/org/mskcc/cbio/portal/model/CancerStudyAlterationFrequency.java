/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.model;

import java.io.Serializable;

/**
 *
 * @author abeshoua
 */
public class CancerStudyAlterationFrequency implements Serializable
{
	public Long entrezGeneId;
	public int internalStudyId;
	public String alterationType;
	public float alterationFrequency;
	public int numCases;
}
