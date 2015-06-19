package pl.skm.smarttag.ahrs;

/**
 * Created by marekmagik on 2015-04-30.
 */
public class QuaternionData {
    /// <summary>
/// Private quaternion array.
/// </summary>
    private float[] quaternion;

/// <summary>
/// Gets or sets the quaternion. Must of 4 elements. Vector will be normalised.
/// </summary>
/// <exception cref="System.Exception">
/// Thrown if invalid quaternion specified.
/// </exception>

    public float[] getQuaternion() {
        return quaternion;
    }

    private void setQuaternion(float[] value) throws Exception {
        if (value.length != 4) {
            throw new Exception("Quaternion vector must be of 4 elements.");
        }
        float norm = (float) Math.sqrt(value[0] * value[0] + value[1] * value[1] + value[2] * value[2] + value[3] *
                value[3]);
        quaternion = value;
        quaternion[0] /= norm;
        quaternion[1] /= norm;
        quaternion[2] /= norm;
        quaternion[3] /= norm;
    }

    /// <summary>
/// Initialises a new instance of the <see cref="QuaternionData"/> class.
/// </summary>
    public QuaternionData() throws Exception {
        this(new float[]{1, 0, 0, 0});
    }

    /// <summary>
/// Initialises a new instance of the <see cref="QuaternionData"/> class.
/// </summary>
/// <param name="quaternion">
/// Quaternion. Must of 4 elements. Each element must be of value -1 to +1.
/// </param>
    public QuaternionData(float[] quaternion) throws Exception {
        setQuaternion(quaternion);
    }

    /// <summary>
/// Returns the quaternion conjugate.
/// </summary>
/// <returns>
/// Quaternion conjugate.
/// </returns>
    public QuaternionData ConvertToConjugate() throws Exception {
        return new QuaternionData(new float[]{quaternion[0], -quaternion[1], -quaternion[2], -quaternion[3]});
    }

    /// <summary>
/// Converts data to rotation matrix.
/// </summary>
/// <remarks>
/// Index order is row major. See http://en.wikipedia.org/wiki/Row-major_order
/// </remarks>
    public float[] ConvertToRotationMatrix() {
        float R11 = 2 * quaternion[0] * quaternion[0] - 1 + 2 * quaternion[1] * quaternion[1];
        float R12 = 2 * (quaternion[1] * quaternion[2] + quaternion[0] * quaternion[3]);
        float R13 = 2 * (quaternion[1] * quaternion[3] - quaternion[0] * quaternion[2]);
        float R21 = 2 * (quaternion[1] * quaternion[2] - quaternion[0] * quaternion[3]);
        float R22 = 2 * quaternion[0] * quaternion[0] - 1 + 2 * quaternion[2] * quaternion[2];
        float R23 = 2 * (quaternion[2] * quaternion[3] + quaternion[0] * quaternion[1]);
        float R31 = 2 * (quaternion[1] * quaternion[3] + quaternion[0] * quaternion[2]);
        float R32 = 2 * (quaternion[2] * quaternion[3] - quaternion[0] * quaternion[1]);
        float R33 = 2 * quaternion[0] * quaternion[0] - 1 + 2 * quaternion[3] * quaternion[3];
        return new float[]{R11, R12, R13,
                R21, R22, R23,
                R31, R32, R33};
    }

    /// <summary>
/// Converts data to ZYX Euler angles (in degrees).
/// </summary>
    public float[] convertToEulerAngles() {
        float phi = (float) Math.atan2(2 * (quaternion[2] * quaternion[3] - quaternion[0] * quaternion[1]),
                2 * quaternion[0] * quaternion[0] - 1 + 2 * quaternion[3] * quaternion[3]);
        float theta = (float) -Math.atan((2.0 * (quaternion[1] * quaternion[3] + quaternion[0] * quaternion[2])) /
                Math.sqrt(1.0 - Math.pow((2.0 * quaternion[1] * quaternion[3] + 2.0 * quaternion[0] * quaternion[2]),
                        2.0)));
        float psi = (float) Math.atan2(2 * (quaternion[1] * quaternion[2] - quaternion[0] * quaternion[3]),
                2 * quaternion[0] * quaternion[0] - 1 + 2 * quaternion[1] * quaternion[1]);
        return new float[]{Rad2Deg(phi), Rad2Deg(theta), Rad2Deg(psi)};
    }

    /// <summary>
/// Converts from radians to degrees.
/// </summary>
/// <param name="radians">
/// Angular quantity in radians.
/// </param>
/// <returns>
/// Angular quantity in degrees.
/// </returns>
    private float Rad2Deg(float radians) {
        return 57.2957795130823f * radians;
    }


}