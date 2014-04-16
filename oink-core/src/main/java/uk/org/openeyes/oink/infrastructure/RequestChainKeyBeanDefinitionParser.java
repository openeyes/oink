/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package uk.org.openeyes.oink.infrastructure;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Triplet;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.HttpMethod;

public class RequestChainKeyBeanDefinitionParser extends RequestMapperBeanDefinitionParser {

	protected Class<?> getBeanClass(Element element) {
		return HttpMapper.class;
	}
	
	protected void doParse(Element element, BeanDefinitionBuilder bean) {
		
		List<Element> mappingElems = DomUtils.getChildElements(element);
		
		List<Triplet<String,HttpMethod,String>> list = new ArrayList<Triplet<String,HttpMethod,String>>();
		
		for(int i = 0; i < mappingElems.size(); i++) {
			Element mappingElem = mappingElems.get(i);
			String resourcePath = mappingElem.getAttribute("resourcePath");
			HttpMethod method = parseMethod(mappingElem.getAttribute("method"));
			String resource = mappingElem.getAttribute("chainKey");
			list.add(new Triplet<String, HttpMethod, String>(resourcePath, method, resource));
		}
		
		bean.addConstructorArgValue(list);
	}
	
}