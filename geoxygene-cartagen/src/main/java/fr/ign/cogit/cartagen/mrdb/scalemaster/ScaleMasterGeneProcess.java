/*******************************************************************************
 * This software is released under the licence CeCILL
 * 
 * see Licence_CeCILL-C_fr.html see Licence_CeCILL-C_en.html
 * 
 * see <a href="http://www.cecill.info/">http://www.cecill.info/a>
 * 
 * @copyright IGN
 ******************************************************************************/
package fr.ign.cogit.cartagen.mrdb.scalemaster;

import java.util.HashSet;
import java.util.Set;

import fr.ign.cogit.cartagen.core.genericschema.IGeneObj;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;

public abstract class ScaleMasterGeneProcess {

  private Set<ProcessParameter> parameters;
  private int scale;

  protected ScaleMasterGeneProcess() {
    parameters = new HashSet<ProcessParameter>();
  }

  public Set<ProcessParameter> getParameters() {
    return parameters;
  }

  public void setParameters(Set<ProcessParameter> parameters) {
    this.parameters = parameters;
  }

  public void setScale(int scale) {
    this.scale = scale;
  }

  public int getScale() {
    return scale;
  }

  /**
   * Fills the process parameters from the generic set of parameters.
   */
  public abstract void parameterise();

  /**
   * Execute the process with the given parameters.
   */
  public abstract void execute(IFeatureCollection<? extends IGeneObj> features)
      throws Exception;

  public abstract String getProcessName();

  /**
   * Get the parameter value from its name.
   * @param paramName
   * @return
   */
  public Object getParamValueFromName(String paramName) {
    for (ProcessParameter param : getParameters()) {
      if (param.getName().equals(paramName))
        return param.getValue();
    }
    return null;
  }

  public boolean hasParameter(String paramName) {
    for (ProcessParameter param : getParameters()) {
      if (param.getName().equals(paramName))
        return true;
    }
    return false;
  }

  public void addParameter(ProcessParameter parameter) {
    this.parameters.add(parameter);
  }

  @Override
  public String toString() {
    StringBuffer buff = new StringBuffer(getProcessName());
    buff.append(" {");
    boolean first = true;
    for (ProcessParameter param : getParameters()) {
      if (!first)
        buff.append(", ");
      first = false;
      buff.append(param.getName());
      buff.append("(" + param.getValue() + ")");
    }
    buff.append("}");
    return buff.toString();
  }

}